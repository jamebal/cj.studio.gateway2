package cj.studio.ecm.net.io;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MemoryContentReciever implements IContentReciever {
	ByteBuf buf;
	boolean isDone;

	public MemoryContentReciever() {
		buf = Unpooled.buffer(8192);
	}

	@Override
	public void begin(Frame frame) {
		isDone=false;
	}

	@Override
	public void recieve(byte[] b, int pos, int length) {
		buf.writeBytes(b, pos, length);
	}

	@Override
	public void done(byte[] b, int pos, int length) {
		buf.writeBytes(b, pos, length);
		isDone = true;
	}

	public byte[] readFully() {
		if (!isDone) {
			throw new EcmException("还没完成");
		}
		byte[] b = new byte[buf.readableBytes()];
		buf.readBytes(b, 0, b.length);
		return b;
	}
}
