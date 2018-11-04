package cj.test.website;

import java.util.Arrays;
import java.util.List;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.layer.ISessionEvent;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.app.GatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;

@CjService(name="$.cj.studio.gateway.app",isExoteric=true)
public class WebsiteProgram extends GatewayAppSiteProgram{
	@Override
	protected List<ISessionEvent> getSessionEvents() {
		
		return Arrays.asList(new ISessionEvent() {

			@Override
			public void doEvent(String action, Object... args) {
				System.out.println("---action---"+action);
			}
			
		});
	}
	@Override
	protected void onstart(Destination dest, String assembliesHome, ProgramAdapterType type) {
		System.out.println("....onstart:"+assembliesHome);
		
	}

}
