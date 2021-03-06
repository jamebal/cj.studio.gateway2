package cj.studio.gateway.socket.serverchannel.udt;

import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.IGatewaySocketContainer;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.IGatewaySocket;
import cj.studio.gateway.socket.serverchannel.AbstractServerChannelSocket;
import cj.studio.gateway.socket.serverchannel.udt.pipeline.builder.UdtServerChannelInputPipelineBuilder;
import cj.studio.gateway.socket.util.SocketName;
import io.netty.channel.Channel;

public class UdtServerChannelGatewaySocket extends AbstractServerChannelSocket implements IGatewaySocket {
	private IServiceProvider parent;
	private Channel channel;
	String gatewayDest;
	public UdtServerChannelGatewaySocket(IServiceProvider parent,String gatewayDest, Channel channel) {
		this.parent = parent;
		this.channel = channel;
		this.gatewayDest=gatewayDest;
	}

	@Override
	public Object getService(String name) {
		if ("$.pipeline.input.builder".equals(name)) {
			return new UdtServerChannelInputPipelineBuilder(parent,channel);
		}
		if ("$.localAddress".equals(name)) {
			return channel.localAddress().toString();
		}
		if ("$.remoteAddress".equals(name)) {
			return channel.remoteAddress().toString();
		}
		return parent.getService(name);
	}

	@Override
	public <T> ServiceCollection<T> getServices(Class<T> clazz) {
		return parent.getServices(clazz);
	}

	@Override
	public String name() {
//		String netName = (String) parent.getService("$.server.name");
		return SocketName.name(channel.id(), gatewayDest);
	}

	@Override
	public void connect(Destination dest) throws CircuitException {
		throw new CircuitException("503", "不支持该方法");
	}

	@Override
	public void close() throws CircuitException {
		IGatewaySocketContainer container = (IGatewaySocketContainer) parent.getService("$.container.socket");
		if (container != null) {
			container.remove(name());
		}
		if (channel.isOpen()) {
			channel.close();
		}
		
	}

}
