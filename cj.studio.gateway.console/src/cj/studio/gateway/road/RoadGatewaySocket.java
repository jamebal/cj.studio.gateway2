package cj.studio.gateway.road;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.Assembly;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IAssembly;
import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.IWorkbin;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.IGatewaySocketContainer;
import cj.studio.gateway.road.pipeline.builder.RoadInputPipelineBuilder;
import cj.studio.gateway.road.pipeline.builder.RoadOutputPipelineBuilder;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.IGatewaySocket;
import cj.studio.gateway.socket.app.FillClassLoader;
import cj.studio.gateway.socket.app.IGatewayAppSitePlugin;
import cj.studio.gateway.socket.app.IGatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.OutputSelector;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

public class RoadGatewaySocket implements IGatewaySocket {

	private IServiceProvider parent;
	private Destination destination;
	private boolean isConnected;
	private String homeDir;
	private IGatewayAppSiteProgram program;

	public RoadGatewaySocket(IServiceProvider parent) {
		this.parent = parent;
		this.homeDir = (String) parent.getService("$.homeDir");
	}
	public boolean isConnected() {
		return isConnected;
	}
	@Override
	public Object getService(String name) {
		if ("$.pipeline.input.builder".equals(name)) {
			return new RoadInputPipelineBuilder(this);
		}
		if ("$.pipeline.output.builder".equals(name)) {
			return new RoadOutputPipelineBuilder(this);
		}
		if ("$.app.program".equals(name)) {
			return program;
		}
		if ("$.socket".equals(name)) {
			return this;
		}
		if ("$.destination".equals(name)) {
			return this.destination;
		}
		if ("$.socket.name".equals(name)) {
			return this.name();
		}
		if ("$.localAddress".equals(name)) {
			return new Gson().toJson(destination.getUris());
		}
		return parent.getService(name);
	}

	@Override
	public <T> ServiceCollection<T> getServices(Class<T> arg0) {
		return parent.getServices(arg0);
	}

	@Override
	public String name() {
		return destination.getName();
	}

	@Override
	public synchronized void connect(Destination dest) throws CircuitException {
		if(isConnected)return;
		// 仅加载app，并不进行连接，留到管道激活时
		this.destination = dest;
		String uri = dest.getName();
		int pos=uri.indexOf("://");
		String protocol = uri.substring(0, pos).trim();
		ProgramAdapterType type = ProgramAdapterType.valueOf(protocol);
		// uri格式是：app://目录:适配类型,如：app://程序的根目录相对于容器下目录assembly的位置:适配类型，例：app://wigo:way
		String appdir = uri.substring(pos + 3, uri.lastIndexOf(":"));

		String sharedir = String.format("%s%slib%sshare", homeDir, File.separator, File.separator);
		ClassLoader share = FillClassLoader.fillShare(sharedir);
		CJSystem.current().environment().logging().info("已装载共享库" + sharedir);

		String assembliesHome = String.format("%s%sroads%s%s%s", homeDir, File.separator, File.separator, appdir,
				File.separator);
		Object prog = scanAssemblyAndLoad(assembliesHome, share);

		IGatewayAppSiteProgram wprog = (IGatewayAppSiteProgram) prog;
		// 初始化会话事件
		wprog.start(dest, assembliesHome, type);
		isConnected = true;
	}
	protected Object scanAssemblyAndLoad(String home, ClassLoader share) throws CircuitException {
		File dir = new File(home);
		if (!dir.exists()) {
			throw new EcmException("程序集目录不存在:" + dir);
		}
		File[] assemblies = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		if (assemblies.length == 0) {
			throw new EcmException("缺少程序集:" + home);
		}
		if (assemblies.length > 1) {
			throw new EcmException("定义了多个程序集:" + home);
		}
		String fn = assemblies[0].getAbsolutePath();
		IAssembly target = Assembly.loadAssembly(fn, share);
		Map<String, IGatewayAppSitePlugin> plugins = scanPluginsAndLoad(home,share);
		target.parent(new AppCoreService(plugins));

		target.start();

		this.program = (IGatewayAppSiteProgram) target.workbin().part("$.cj.studio.gateway.road");

		if (program == null) {
			throw new EcmException("程序集验证失败，原因：未发现Program的派生实现");
		}

		String expire = target.info().getProperty("site.session.expire");
		if (StringUtil.isEmpty(expire)) {
			expire = (30 * 60 * 1000L) + "";
		}
		return program;
	}
	private Map<String, IGatewayAppSitePlugin> scanPluginsAndLoad(String assemblyHome, ClassLoader share) {
		String dir=assemblyHome;
		if(!dir.endsWith(File.separator)) {
			dir+=File.separator;
		}
		dir=String.format("%splugins", dir);
		File dirFile=new File(dir);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File[] pluginDirs=dirFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		Map<String, IGatewayAppSitePlugin> map=new HashMap<>();
		if(pluginDirs.length==0) {
			return map;
		}
		for(File f:pluginDirs) {
			File[] pluginFiles=f.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			for(File pluginFile:pluginFiles) {
				IAssembly assembly=Assembly.loadAssembly(pluginFile.getAbsolutePath(),share);
				assembly.start();
				IWorkbin bin=assembly.workbin();
				IGatewayAppSitePlugin plugin=(IGatewayAppSitePlugin)bin.part("$.studio.gateway.app.plugin");
				if(plugin!=null) {
					String name=bin.chipInfo().getName();
					map.put(name, plugin);
				}
			}
		}
		return map;
	}
	
	@Override
	public void close() throws CircuitException {
		IGatewaySocketContainer container = (IGatewaySocketContainer) parent.getService("$.container.socket");
		if (container != null) {
			container.remove(name());
		}
		isConnected = false;
		program.close();
		this.parent = null;
		this.program = null;
	}
	class AppCoreService implements IServiceProvider {
		IOutputSelector selector;
		Map<String, IGatewayAppSitePlugin> plugins;

		public AppCoreService(Map<String, IGatewayAppSitePlugin> plugins) {
			this.plugins = plugins;
		}

		@Override
		public Object getService(String name) {
			if ("$.output.selector".equals(name)) {
				if (selector == null) {
					selector = new OutputSelector(RoadGatewaySocket.this);
				}
				return selector;
			}
			if (!plugins.isEmpty()) {
				int pos = name.indexOf(".");
				if (pos > 0) {
					String key = name.substring(0, pos);
					String sid = name.substring(pos + 1, name.length());
					IGatewayAppSitePlugin plugin = plugins.get(key);
					if (plugin == null)
						return null;
					Object obj = plugin.getService(sid);
					if (obj != null)
						return obj;
				}
			}
			return null;
		}

		@Override
		public <T> ServiceCollection<T> getServices(Class<T> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
