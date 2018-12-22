package cj.studio.gateway.stub;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.gateway.socket.util.SocketContants;
import cj.studio.gateway.stub.annotation.CjStubInContent;
import cj.studio.gateway.stub.annotation.CjStubInHead;
import cj.studio.gateway.stub.annotation.CjStubInParameter;
import cj.studio.gateway.stub.annotation.CjStubMethod;
import cj.studio.gateway.stub.annotation.CjStubService;
import cj.studio.gateway.stub.util.StringTypeConverter;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

public class GatewayAppSiteRestStub implements IGatewayAppSiteWayWebView, StringTypeConverter {
	public GatewayAppSiteRestStub() {
		CjService cj = this.getClass().getAnnotation(CjService.class);
		if (cj == null) {
			throw new EcmException("必须定义为服务");
		}
		checkError(cj.name(), this.getClass());
	}

	private void checkError(String name, Class<?> clazz) {
		CjStubService found = null;
		do {
			Class<?>[] faces = clazz.getInterfaces();

			for (Class<?> c : faces) {
				CjStubService an = c.getDeclaredAnnotation(CjStubService.class);
				if (an != null) {
					found = an;
					break;
				}
			}
			clazz = clazz.getSuperclass();
		} while (clazz.equals(Object.class));
		if (found == null) {
			throw new EcmException("没有发现存根接口");
		}
		if (!name.startsWith(found.bindService()) && !found.bindService().startsWith(name)) {
			throw new EcmException("存根接口绑定服务名与宿主服务名不同");
		}
	}

	@Override
	public final void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
		frame.content().accept(new MemoryContentReciever() {
			@Override
			public void done(byte[] b, int pos, int length) throws CircuitException {
				super.done(b, pos, length);
				String restCmd = frame.head(SocketContants.__frame_Head_Rest_Command);
				if (StringUtil.isEmpty(restCmd)) {
					restCmd = frame.parameter(SocketContants.__frame_Head_Rest_Command);
				}
				String stubClassName = frame.head(SocketContants.__frame_Head_Rest_Stub_Interface);
				if (StringUtil.isEmpty(stubClassName)) {
					stubClassName = frame.parameter(SocketContants.__frame_Head_Rest_Stub_Interface);
				}
				Class<?> clazz = GatewayAppSiteRestStub.this.getClass();
				try {
					Class<?> stub = Class.forName(stubClassName, true, clazz.getClassLoader());
					if (!stub.isAssignableFrom(clazz)) {
						throw new CircuitException("503", "当前webview未实现存根接口。" + stub + " 在 " + clazz);
					}
					Method src = findMethod(restCmd, stub);
					if (src == null) {
						throw new CircuitException("404", "在存根接口中未找到方法：" + src);
					}
					Method dest = findDestMethod(clazz, src);
					if (dest == null) {
						throw new CircuitException("404", "在webview中未找到方法：" + dest);
					}
					Object[] args = getArgs(src, frame);
					Object ret = dest.invoke(GatewayAppSiteRestStub.this, args);
					if (ret != null) {
						circuit.content().writeBytes(new Gson().toJson(ret).getBytes());
					}
				} catch (Exception e) {
					if (e instanceof CircuitException) {
						throw (CircuitException) e;
					}
					if (e instanceof InvocationTargetException) {
						InvocationTargetException inv=(InvocationTargetException)e;
						throw new CircuitException("503", inv.getTargetException());
					}
					throw new CircuitException("503", e);
				}
			}
		});

	}

	private Object[] getArgs(Method src, Frame frame) throws CircuitException {
		Parameter[] arr = src.getParameters();
		Object[] args = new Object[arr.length];
		for (int i = 0; i < arr.length; i++) {
			Parameter p = arr[i];
			CjStubInHead sih = p.getAnnotation(CjStubInHead.class);
			if (sih != null) {
				String value = frame.head(sih.key());
				try {
					value = URLDecoder.decode(value, "utf-8");
				} catch (UnsupportedEncodingException e) {
				}
				args[i] = convertFrom(p.getType(), value);
				continue;
			}
			CjStubInParameter sip = p.getAnnotation(CjStubInParameter.class);
			if (sip != null) {
				try {
					String value = frame.parameter(sip.key());
					value = URLDecoder.decode(value, "utf-8");
					args[i] = convertFrom(p.getType(), value);
				} catch (UnsupportedEncodingException e) {
				}
				continue;
			}
			CjStubInContent sic = p.getAnnotation(CjStubInContent.class);
			if (sic != null) {
				byte[] b = frame.content().readFully();
				Object value = new Gson().fromJson(new String(b), p.getType());
				args[i] = value;
				continue;
			}
		}

		return args;
	}

	private Method findDestMethod(Class<?> clazz, Method src) throws NoSuchMethodException, SecurityException {
		Method m = null;
		try {
			m = clazz.getDeclaredMethod(src.getName(), src.getParameterTypes());
		} catch (NoSuchMethodException e) {
			if (!Object.class.equals(clazz)) {
				Class<?> superC = clazz.getSuperclass();
				m = findDestMethod(superC, src);
			}
		}
		return m;
	}

	private Method findMethod(String restCmd, Class<?> stub) {
		Method[] arr = stub.getDeclaredMethods();
		for (Method m : arr) {
			CjStubMethod cm = m.getAnnotation(CjStubMethod.class);
			if (cm == null) {
				continue;
			}
			String methodName = cm.alias();
			if (StringUtil.isEmpty(methodName)) {
				methodName = m.getName();
			}
			if (restCmd.equals(methodName)) {
				return m;
			}
		}
		return null;
	}
}