package cj.studio.orm.mybatis;

import java.lang.reflect.Method;

import org.apache.ibatis.session.SqlSession;

import cj.ultimate.net.sf.cglib.proxy.InvocationHandler;

class MappingCallback implements InvocationHandler {
	ISafeSqlSessionFactory factory;
	private Class<?> interfaceClazz;
	public MappingCallback(ISafeSqlSessionFactory factory, Class<?> clazz) {
		this.factory=factory;
		this.interfaceClazz=clazz;
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		SqlSession session=factory.getSession();//在执行mapping方法前事务方面已为当前线程设会话了，因此此处肯定是仅仅是返回会话
		Object obj=session.getMapper(interfaceClazz);
		Object ret=m.invoke(obj, args);
		return ret;
	}

}