package cj.studio.gateway.socket.serverchannel.udt.valve;

import cj.studio.ecm.frame.Frame;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.gateway.socket.pipeline.IInputValve;
import io.netty.channel.Channel;
import io.netty.channel.udt.UdtMessage;

public class LastUdtServerChannelInputValve implements IInputValve {
	Channel channel;
	public LastUdtServerChannelInputValve(Channel channel) {
		this.channel=channel;
	}

	@Override
	public void onActive(String inputName,IIPipeline pipeline)
			throws CircuitException {
		
	}

	@Override
	public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
		if(!(request instanceof Frame) ){
			throw new CircuitException("505", "不支持的请求消息类型:"+request);
		}
		if(!channel.isWritable()) {
			throw new CircuitException("505", "对点网络已不可写，无法处理回推侦："+request);
		}
		Frame frame=(Frame)request;
		UdtMessage okmsg = new UdtMessage(frame.toByteBuf());
		channel.writeAndFlush(okmsg);
	}

	@Override
	public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
		channel.close();
	}

}
