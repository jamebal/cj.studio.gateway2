package cj.studio.gateway.socket.valve;

import cj.studio.ecm.graph.CircuitException;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.gateway.socket.pipeline.IInputValve;

public class FirstNetInputValve implements IInputValve{

	@Override
	public void onActive(String inputName, Object request, Object response, IIPipeline pipeline)
			throws CircuitException {
		pipeline.nextOnActive(inputName, request, response, this);
		
	}

	@Override
	public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
		pipeline.nextFlow(request, response, this);
		
	}

	@Override
	public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
		pipeline.nextOnInactive(inputName, this);
		
	}

	

}
