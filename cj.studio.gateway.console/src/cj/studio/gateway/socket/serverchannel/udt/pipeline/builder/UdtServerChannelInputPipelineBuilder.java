package cj.studio.gateway.socket.serverchannel.udt.pipeline.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.graph.CircuitException;
import cj.studio.gateway.socket.pipeline.IInputPipeline;
import cj.studio.gateway.socket.pipeline.IInputPipelineBuilder;
import cj.studio.gateway.socket.pipeline.IInputValve;
import cj.studio.gateway.socket.pipeline.InputPipeline;
import cj.studio.gateway.socket.serverchannel.udt.valve.FirstUdtServerChannelInputValve;
import cj.studio.gateway.socket.serverchannel.udt.valve.LastUdtServerChannelInputValve;
import cj.studio.gateway.socket.util.SocketContants;
import io.netty.channel.Channel;

public class UdtServerChannelInputPipelineBuilder implements IInputPipelineBuilder {
	private Map<String, Object> props;
	private Channel channel;
	String name;
	private IServiceProvider parent;
	
	public UdtServerChannelInputPipelineBuilder(IServiceProvider parent, Channel channel) {
		this.channel=channel;
		this.parent=parent;
		
	}
	@Override
	public IInputPipelineBuilder name(String name) {
		this.name=name;
		return this;
	}

	@Override
	public IInputPipelineBuilder prop(String name, Object value) {
		if(props==null) {
			props=new HashMap<>();
		}
		props.put(name, value);
		return this;
	}

	@Override
	public IInputPipeline createPipeline() throws CircuitException {
		IInputValve first=new FirstUdtServerChannelInputValve();
		IInputValve last=new LastUdtServerChannelInputValve(channel);
		IInputPipeline input=new InputPipeline(first, last);
		
		input.prop(SocketContants.__pipeline_name, name);
		String toWho=(String)parent.getService("$.server.name");
		input.prop(SocketContants.__pipeline_toWho,toWho);
		input.prop(SocketContants.__pipeline_toProtocol, "udt");
		if(props!=null) {
			Set<String> set=props.keySet();
			for(String key:set) {
				input.prop(key,(String) props.get(key));
			}
		}
		return input;
	}

}
