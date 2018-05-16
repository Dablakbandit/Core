package me.dablakbandit.core.nbt;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class NBTReader implements Closeable{
	private final DataInputStream	is;
	
	private int						nextType	= -1;
	
	private String					nextName	= null;
	
	private ArrayList<Integer>		depthItems	= new ArrayList<Integer>();
	
	private ArrayList<Integer>		depthType	= new ArrayList<Integer>();
	
	private int						depth		= -1;
	
	@Deprecated
	public NBTReader(InputStream is) throws IOException{
		this(is, NBTCompression.GZIP);
	}
	
	@Deprecated
	public NBTReader(InputStream is, boolean gzipped) throws IOException{
		this(is, gzipped ? NBTCompression.GZIP : NBTCompression.UNCOMPRESSED);
	}
	
	public NBTReader(InputStream is, NBTCompression compression) throws IOException{
		NBTCompression resolvedCompression;
		if(compression == NBTCompression.FROM_BYTE){
			int compressionByte = is.read();
			if(compressionByte < 0){ throw new EOFException(); }
			resolvedCompression = NBTCompression.fromId(compressionByte);
		}else{
			resolvedCompression = compression;
		}
		
		switch(resolvedCompression){
		case UNCOMPRESSED:
			this.is = new DataInputStream(is);
			break;
		case GZIP:
			this.is = new DataInputStream(new GZIPInputStream(is));
			break;
		case ZLIB:
			this.is = new DataInputStream(new InflaterInputStream(is));
			break;
		case FROM_BYTE:
			throw new AssertionError("FROM_BYTE Should have been handled already");
		default:
			throw new AssertionError("[JNBT] Unimplemented " + NBTCompression.class.getSimpleName() + ": " + compression);
		}
	}
	
	public String nextName() throws IOException{
		nextType();
		
		if(this.nextName != null){ return this.nextName; }
		
		if(this.nextType == NBTConstants.TYPE_END){
			this.nextName = "";
		}else{
			int nameLength = is.readShort() & 0xFFFF;
			byte[] nameBytes = new byte[nameLength];
			is.readFully(nameBytes);
			this.nextName = new String(nameBytes, NBTConstants.CHARSET);
		}
		return this.nextName;
	}
	
	public int nextType() throws IOException{
		// Cache the type of tag once it has been read.
		if(this.nextType == -1){
			this.nextType = is.readByte() & 0xFF;
		}
		return this.nextType;
	}
	
	public byte nextByte() throws IOException{
		next();
		return is.readByte();
	}
	
	public short nextShort() throws IOException{
		next();
		return is.readShort();
	}
	
	public int nextInt() throws IOException{
		next();
		return is.readInt();
	}
	
	public long nextLong() throws IOException{
		next();
		return is.readLong();
	}
	
	public float nextFloat() throws IOException{
		next();
		return is.readFloat();
	}
	
	public double nextDouble() throws IOException{
		next();
		return is.readDouble();
	}
	
	public byte[] nextByteArray() throws IOException{
		next();
		int length = is.readInt();
		byte[] data = new byte[length];
		is.readFully(data);
		return data;
	}
	
	public String nextString() throws IOException{
		next();
		int length = is.readShort();
		byte[] bytes = new byte[length];
		is.readFully(bytes);
		return new String(bytes, NBTConstants.CHARSET);
	}
	
	public int[] nextIntArray() throws IOException{
		next();
		int length = is.readInt();
		int[] array = new int[length];
		for(int i = 0; i < length; i++)
			array[i] = is.readInt();
		return array;
	}
	
	private void next() throws IOException, IllegalStateException{
		this.nextName = null;
		int itemsLeft = (this.depth < 0 ? -1 : getRemainingItems());
		if(itemsLeft > 0){
			itemsLeft--;
			this.depthItems.set(this.depth, itemsLeft);
			this.nextType = this.depthType.get(this.depth);
		}else if(itemsLeft == 0){
			throw new IllegalStateException("[JNBT] Attempted to read next element from a list with no remaining elements!");
		}else{
			this.nextType = -1;
		}
	}
	
	private int getRemainingItems(){
		return this.depthItems.get(this.depth);
	}
	
	public void beginObject(){
		// Reset tag name and type caches.
		this.nextName = null;
		this.nextType = -1;
		// -1 to internally indicate that the tag at this depth is a TAG_Compound.
		this.depthItems.add(-1);
		this.depthType.add(-1);
		// Increase the depth at which we are currently reading.
		this.depth++;
	}
	
	public void beginArray() throws IOException{
		// Reset tag name. (Elements in a TAG_List have no names.)
		this.nextName = null;
		// Determine the type and number of tags this list contains.
		int type = is.readByte();
		int length = is.readInt();
		this.nextType = type;
		// Internally indicate the number of items that this list contains, to
		// ensure that no more, and no less, than this number of children will be
		// read from this TAG_List.
		this.depthItems.add(length);
		// Internally indicate the type of list that is present at this structure depth.
		this.depthType.add(type);
		// Increase the depth at which we are currently reading.
		this.depth++;
	}
	
	public boolean hasNext() throws IOException, IllegalStateException{
		if(this.depth < 0){
			// This method was called at root level
			throw new IllegalStateException("[JNBT] hasNext() cannot be called at the root level!");
		}
		int itemsLeft = getRemainingItems();
		// [--TAG_List--] [---------------------TAG_Compound---------------------]
		return itemsLeft > 0 || (itemsLeft == -1 && nextType() != NBTConstants.TYPE_END);
	}
	
	public void endArray() throws IOException, IllegalStateException{
		int itemsLeft = getRemainingItems();
		if(itemsLeft == -1){
			throw new IllegalStateException("[JNBT] Attempted to end an object using endArray()!");
		}else if(itemsLeft > 0){ throw new IllegalStateException("[JNBT] Attempted to end an array prematurely!"); }
		this.depth--;
		this.depthItems.remove(this.depthItems.size() - 1);
		this.depthType.remove(this.depthType.size() - 1);
		next();
	}
	
	public void endObject() throws IOException, IllegalStateException{
		int itemsLeft = getRemainingItems();
		if(itemsLeft != -1){ throw new IllegalStateException("[JNBT] Attempted to end an array using endObject()!"); }
		if(this.nextType != NBTConstants.TYPE_END){ throw new IllegalStateException("[JNBT] Attempted to end an object prematurely!"); }
		this.depth--;
		this.depthItems.remove(this.depthItems.size() - 1);
		this.depthType.remove(this.depthType.size() - 1);
		next();
	}
	
	public void skipValue() throws IOException{
		skipValue(this.nextType);
		next();
	}
	
	private void skipValue(int type) throws IOException{
		// Number of bytes that should be skipped over in the input stream.
		int length = 0;
		
		switch(type){
		case NBTConstants.TYPE_END:
			length = 0;
			break;
		
		case NBTConstants.TYPE_BYTE:
			length = 1;
			break;
		
		case NBTConstants.TYPE_SHORT:
			length = 2;
			break;
		
		case NBTConstants.TYPE_INT:
		case NBTConstants.TYPE_FLOAT:
			length = 4;
			break;
		
		case NBTConstants.TYPE_LONG:
		case NBTConstants.TYPE_DOUBLE:
			length = 8;
			break;
		
		case NBTConstants.TYPE_BYTE_ARRAY:
			length = is.readInt();
			break;
		
		case NBTConstants.TYPE_STRING:
			length = is.readUnsignedShort();
			break;
		
		case NBTConstants.TYPE_LIST:
			int listType = is.readByte();
			int listLength = is.readInt();
			for(int i = 0; i < listLength; i++){
				skipValue(listType);
			}
			length = 0;
			break;
		
		case NBTConstants.TYPE_COMPOUND:
			int compType = is.readByte() & 0xFF;
			while(compType != NBTConstants.TYPE_END){
				int nameLength = is.readShort() & 0xFFFF;
				
				// Instead of using is.skip, the bytes are read using readFully()
				// which ensures that all of the required bytes are actually read.
				// is.skip may skip fewer than requested bytes in some cases,
				// particularly when is wraps around a network input.
				byte[] skip = new byte[nameLength];
				is.readFully(skip);
				
				skipValue(compType);
				compType = is.readByte() & 0xFF;
			}
			length = 0;
			break;
		
		case NBTConstants.TYPE_INT_ARRAY:
			length = is.readInt() * 4;
			break;
		
		default:
			throw new IOException("[JNBT] Invalid tag type: " + this.nextType + '.');
		}
		
		// Instead of using is.skip, the bytes are read using readFully() which ensures
		// that all of the required bytes are actually read. is.skip may skip fewer than
		// requested bytes in some cases, particularly when is wraps around a network input.
		while(length > 0){
			int delta = Math.min(length, 32768);
			byte[] discard = new byte[delta];
			is.readFully(discard);
			length -= delta;
		}
	}
	
	@Override
	public void close() throws IOException{
		is.close();
	}
}
