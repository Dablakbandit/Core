package me.dablakbandit.core.utils.itemutils;

import me.dablakbandit.core.json.JSONArray;
import me.dablakbandit.core.json.JSONObject;
import me.dablakbandit.core.nbt.NBTConstants;
import me.dablakbandit.core.utils.ItemUtils;
import me.dablakbandit.core.utils.NMSUtils;
import me.dablakbandit.core.utils.Version;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class DefaultItemUtils implements IItemUtils{

	public Material getMaterial(String... possible){
		for(String s : possible){
			try{
				Material m = Material.valueOf(s);
				if(m != null){ return m; }
			}catch(Exception e){
			}
		}
		return null;
	}
	
	public boolean banner = getBanner();
	
	public boolean getBanner(){
		try{
			Material m = getMaterial("BANNER", "BLACK_BANNER");
			if(m != null){ return true; }
		}catch(Exception e){
		}
		return false;
	}
	
	public boolean hasBanner(){
		return banner;
	}
	
	public Class<?>	nmis	= NMSUtils.getClassSilent("net.minecraft.world.item.ItemStack"), cis = NMSUtils.getOBCClass("inventory.CraftItemStack");
	public Method	nmscopy	= NMSUtils.getMethodSilent(cis, "asNMSCopy", ItemStack.class);
	
	public Class<?> getNMSItemClass(){
		return nmis;
	}
	
	public Object getNMSCopy(ItemStack is) throws Exception{
		return nmscopy.invoke(null, is);
	}
	
	public Method hastag = NMSUtils.getMethodSilent(nmis, new String[]{"e","hasTag"});
	
	public boolean hasTag(Object is) throws Exception{
		return (boolean)hastag.invoke(is);
	}
	
	public Method acm = NMSUtils.getMethodSilent(cis, "asCraftMirror", nmis);
	
	public ItemStack asCraftMirror(Object nis) throws Exception{
		return (ItemStack)acm.invoke(null, nis);
	}
	
	public Method abc = NMSUtils.getMethodSilent(cis, "asBukkitCopy", nmis);
	
	public ItemStack asBukkitCopy(Object nmis) throws Exception{
		return (ItemStack)abc.invoke(null, nmis);
	}
	
	public Class<?>	ni	= NMSUtils.getClassSilent("net.minecraft.world.item.Item");
	
	public Method	gn	= NMSUtils.getMethodSilent(nmis, new String[]{"v", "getName"});
	
	public String getName(ItemStack is){
		try{
			return (String)gn.invoke(getItem(getNMSCopy(is)));
		}catch(Exception e){
			return null;
		}
	}
	
	public Method gi = NMSUtils.getMethodReturnSilent(nmis, ni), ia = getA();
	
	public Object getItem(Object nis) throws Exception{
		return gi.invoke(nis);
	}
	
	public Method getA(){
		Method m = NMSUtils.getMethodSilent(ni, "a", nmis);
		if(m == null){
			m = NMSUtils.getMethodSilent(ni, "n", nmis);
		}
		return m;
	}
	
	private Object empty = getEmptyValue();
	
	private Object getEmptyValue(){
		try{
			Field empty = NMSUtils.getFirstFieldOfTypeWithException(nmis, nmis);
			return empty.get(null);
		}catch(Exception e){
			try{
				Constructor<?> con = NMSUtils.getConstructorSilent(nmis);
				return con.newInstance();
			}catch(Exception e1){
				return null;
			}
		}
	}
	
	public Object getEmpty(){
		return empty;
	}
	
	public String getRawName(ItemStack is){
		try{
			Object nis = getNMSCopy(is);
			return (String)ia.invoke(gi.invoke(nis), nis);
		}catch(Exception e){
			return null;
		}
	}
	
	public Method gin = NMSUtils.getMethodSilent(ni, new String[]{"a", "getName"});
	
	public String getItemName(ItemStack is){
		try{
			return (String)gin.invoke(gi.invoke(getNMSCopy(is)));
		}catch(Exception e){
			return null;
		}
	}
	
	public Object registry = getRegistry();
	
	public Object getRegistry(){
		try{
			return NMSUtils.getFieldSilent(NMSUtils.getClassSilent("net.minecraft.core.IRegistry"), "e").get(null);
		}catch(Exception e){
		}
		return null;
	}
	
	public Class<?> nmrs = getRegistryMaterials();
	
	private Class<?> getRegistryMaterials(){
		return NMSUtils.getClassSilent("net.minecraft.core.RegistryMaterials");
	}
	
	public Field nmrsc = getRegistryMap();

	private Field getRegistryMap(){
		Field field = NMSUtils.getFieldSilent(nmrs, "bx");
		if(field == null){
			field = NMSUtils.getFieldSilent(nmrs, "bB");
		}
		if(field == null){
			field = NMSUtils.getFieldSilent(nmrs, "bU");
		}
		if(field == null){
			field = NMSUtils.getFieldSilent(nmrs, "f");
		}
		return field;
	}
	
	public String getMinecraftName(ItemStack is){
		String name = getItemName(is);
		try{
			Map<?, ?> m = (Map<?, ?>)nmrsc.get(registry);
			for(Entry<?, ?> e : m.entrySet()){
				Object item = e.getValue();
				String s = (String)gin.invoke(item);
				if(name.equals(s)){ return e.getKey().toString(); }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<?>	nbttc	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagCompound");
	public Field	tag		= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nmis, nbttc);
	
	public Object getTag(Object is) throws Exception{
		return tag.get(is);
	}
	
	public void setTag(Object is, Object tag1) throws Exception{
		tag.set(is, tag1);
	}
	
	public Method nbtcie = NMSUtils.getMethodReturnSilent(nbttc, new String[]{"g", "f", "isEmpty"}, boolean.class);
	
	public boolean isEmpty(Object tag) throws Exception{
		return (boolean)nbtcie.invoke(tag);
	}
	
	public Field nbttcm = NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbttc, Map.class);
	
	public Object getMap(Object tag) throws Exception{
		return nbttcm.get(tag);
	}
	
	public Class<?>	nbtb		= NMSUtils.getClassSilent("net.minecraft.nbt.NBTBase");
	public Method	nbttcs		= NMSUtils.getMethodSilent(nbttc, new String[]{"a", "set"}, String.class, nbtb);
	public Method	nbttcr		= NMSUtils.getMethodSilent(nbttc, new String[]{"r", "remove"}, String.class);
	public Method	nbttcss		= NMSUtils.getMethodSilent(nbttc, new String[]{"a", "setString"}, String.class, String.class);
	public Method	nbttcsi		= NMSUtils.getMethodSilent(nbttc, new String[]{"a", "setInt"}, String.class, int.class);
	public Method	nbttcsd		= NMSUtils.getMethodSilent(nbttc, new String[]{"a", "setDouble"}, String.class, double.class);
	public Method	nbttcsl		= NMSUtils.getMethodSilent(nbttc, new String[]{"a", "setLong"}, String.class, long.class);
	public Method	nbttcss1	= NMSUtils.getMethodSilent(nbttc, new String[]{"a", "setShort"}, String.class, short.class);
	
	public void remove(Object tag, String key) throws Exception{
		nbttcr.invoke(tag, key);
	}
	
	public void set(Object tag, String key, Object value) throws Exception{
		nbttcs.invoke(tag, key, value);
	}
	
	public void setString(Object tag, String key, String value) throws Exception{
		nbttcss.invoke(tag, key, value);
	}
	
	public void setShort(Object tag, String key, short value) throws Exception{
		nbttcss1.invoke(tag, key, value);
	}
	
	public void setInt(Object tag, String key, int i) throws Exception{
		nbttcsi.invoke(tag, key, i);
	}
	
	public void setDouble(Object tag, String key, double d) throws Exception{
		nbttcsd.invoke(tag, key, d);
	}
	
	public void setLong(Object tag, String key, long l) throws Exception{
		nbttcsl.invoke(tag, key, l);
	}
	
	public Method nbttchk = NMSUtils.getMethodSilent(nbttc, new String[]{"e", "hasKey"}, String.class);
	
	public boolean hasKey(Object tag, String key) throws Exception{
		return (boolean)nbttchk.invoke(tag, key);
	}
	
	public Method	nbttcg		= NMSUtils.getMethodSilent(nbttc, new String[]{"c", "get"}, String.class);
	public Method	nbttcgs		= NMSUtils.getMethodSilent(nbttc, new String[]{"l", "getString"}, String.class);
	public Method	nbttcgi		= NMSUtils.getMethodSilent(nbttc, new String[]{"h", "getInt"}, String.class);
	public Method	nbttcgd		= NMSUtils.getMethodSilent(nbttc, new String[]{"k", "getDouble"}, String.class);
	public Method	nbttcgl		= NMSUtils.getMethodSilent(nbttc, new String[]{"i", "getLong"}, String.class);
	public Method	nbttcgs1	= NMSUtils.getMethodSilent(nbttc, new String[]{"g", "getShort"}, String.class);
	
	public Object get(Object tag, String key) throws Exception{
		return nbttcg.invoke(tag, key);
	}
	
	public String getString(Object tag, String key) throws Exception{
		return (String)nbttcgs.invoke(tag, key);
	}
	
	public int getInt(Object tag, String key) throws Exception{
		return (int)nbttcgi.invoke(tag, key);
	}
	
	public double getDouble(Object tag, String key) throws Exception{
		return (double)nbttcgd.invoke(tag, key);
	}
	
	public long getLong(Object tag, String key) throws Exception{
		return (long)nbttcgl.invoke(tag, key);
	}
	
	public short getShort(Object tag, String key) throws Exception{
		return (short)nbttcgs1.invoke(tag, key);
	}
	
	public Constructor<?> nbttcc = NMSUtils.getConstructorSilent(nbttc);
	
	public Object getNewNBTTagCompound() throws Exception{
		return nbttcc.newInstance();
	}
	
	public Method hkot = NMSUtils.getMethodSilent(nbttc, new String[]{"b", "hasKeyOfType"}, String.class, int.class);
	
	public boolean hasAttributeModifiersKey(Object tag) throws Exception{
		return (boolean)hkot.invoke(tag, "AttributeModifiers", 9);
	}
	
	public Class<?>			nbttl	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagList");
	public Method			gl		= NMSUtils.getMethodSilent(nbttc, new String[]{"c","getList"}, String.class, int.class);
	public Method			gb		= NMSUtils.getMethodSilent(nbttc, new String[]{"q","getBoolean"}, String.class);
	public Method			sb		= NMSUtils.getMethod(nbttc, new String[]{"a", "setBoolean"}, String.class, boolean.class);
	public Method			nbttla	= NMSUtils.getMethodSilent(nbttl, new String[]{"a", "add"}, nbtb);
	public Constructor<?>	nbttlc	= NMSUtils.getConstructorSilent(nbttl);
	
	public Object getList(Object tag) throws Exception{
		return gl.invoke(tag, "AttributeModifiers", 9);
	}
	
	public Object getList(Object tag, String name, int id) throws Exception{
		return gl.invoke(tag, name, id);
	}
	
	public boolean getUnbreakable(Object tag) throws Exception{
		return (boolean)gb.invoke(tag, "Unbreakable");
	}
	
	public void setUnbreakable(Object tag, boolean value) throws Exception{
		sb.invoke(tag, "Unbreakable", value);
	}
	
	public Object getNewNBTTagList() throws Exception{
		return nbttlc.newInstance();
	}
	
	public void addToList(Object taglist, Object nbt) throws Exception{
		nbttla.invoke(taglist, nbt);
	}
	
	public Method gs = NMSUtils.getMethodSilent(nbttl, "size");
	
	public int getSize(Object list) throws Exception{
		return (int)gs.invoke(list);
	}
	
	public Method g = NMSUtils.getMethodSilent(nbttl, "get", int.class);
	
	public Object get(Object tlist, int i) throws Exception{
		return g.invoke(tlist, i);
	}
	
	public Method			gti		= NMSUtils.getMethodReturn(nbtb, byte.class);

	public Class<?>			nbtby	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagByte");
	public Class<?>			nbtba	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagByteArray");
	public Class<?>			nbtd	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagDouble");
	public Class<?>			nbtf	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagFloat");
	public Class<?>			nbti	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagInt");
	public Class<?>			nbtia	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagIntArray");
	public Class<?>			nbtla	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagLongArray");
	public Class<?>			nbtl	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagList");
	public Class<?>			nbtlo	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagLong");
	public Class<?>			nbts	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagShort");
	public Class<?>			nbtst	= NMSUtils.getClassSilent("net.minecraft.nbt.NBTTagString");
	
	public Constructor<?>	nbtbc	= NMSUtils.getConstructorSilent(nbtby, byte.class);
	public Constructor<?>	nbtbac	= NMSUtils.getConstructorSilent(nbtba, byte[].class);
	public Constructor<?>	nbtdc	= NMSUtils.getConstructorSilent(nbtd, double.class);
	public Constructor<?>	nbtfc	= NMSUtils.getConstructorSilent(nbtf, float.class);
	public Constructor<?>	nbtic	= NMSUtils.getConstructorSilent(nbti, int.class);
	public Constructor<?>	nbtiac	= NMSUtils.getConstructorSilent(nbtia, int[].class);
	public Constructor<?>	nbtlac	= NMSUtils.getConstructorSilent(nbtla, long[].class);
	public Constructor<?>	nbtlc	= NMSUtils.getConstructorSilent(nbtl);
	public Constructor<?>	nbtloc	= NMSUtils.getConstructorSilent(nbtlo, long.class);
	public Constructor<?>	nbtsc	= NMSUtils.getConstructorSilent(nbts, short.class);
	public Constructor<?>	nbtstc	= NMSUtils.getConstructorSilent(nbtst, String.class);
	
	public Field			nbtbd	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtby, byte.class);
	public Field			nbtbad	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtba, byte[].class);
	public Field			nbtdd	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtd, double.class);
	public Field			nbtfd	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtf, float.class);
	public Field			nbtid	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbti, int.class);
	public Field			nbtiad	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtia, int[].class);
	public Field			nbtlad	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtla, long[].class);
	public Field			nbtld	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtl, List.class);
	public Field			nbtlt	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtl, byte.class);
	public Field			nbttcd	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbttc, Map.class);
	public Field			nbtlod	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtlo, long.class);
	public Field			nbtsd	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbts, short.class);
	public Field			nbtstd	= NMSUtils.getFirstNonStaticFieldOfTypeSilent(nbtst, String.class);
	
	public Object getNewNBTTagByte(byte value) throws Exception{
		return nbtbc.newInstance(value);
	}
	
	public Object getNewNBTTagByteArray(byte[] value) throws Exception{
		return nbtbac.newInstance(value);
	}
	
	public Object getData(Object nbt) throws Exception{
		int i = (int)((byte)gti.invoke(nbt));
		switch(i){
		case NBTConstants.TYPE_BYTE:
			return nbtbd.get(nbt);
		case NBTConstants.TYPE_SHORT:
			return nbtsd.get(nbt);
		case NBTConstants.TYPE_INT:
			return nbtid.get(nbt);
		case NBTConstants.TYPE_LONG:
			return nbtlod.get(nbt);
		case NBTConstants.TYPE_FLOAT:
			return nbtfd.get(nbt);
		case NBTConstants.TYPE_DOUBLE:
			return nbtdd.get(nbt);
		case NBTConstants.TYPE_BYTE_ARRAY:
			return nbtbad.get(nbt);
		case NBTConstants.TYPE_STRING:
			return nbtstd.get(nbt);
		case NBTConstants.TYPE_LIST:
			return convertListTagToValueList(nbt);
		case NBTConstants.TYPE_COMPOUND:
			return convertCompoundTagToValueMap(nbt);
		case NBTConstants.TYPE_INT_ARRAY:
			return nbtiad.get(nbt);
		case NBTConstants.TYPE_LONG_ARRAY:
			return nbtlad.get(nbt);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Object createData(Object value) throws Exception{
		if(value.getClass().equals(byte.class)){ return nbtbc.newInstance(value); }
		if(value.getClass().equals(byte[].class)){ return nbtbac.newInstance(value); }
		if(value.getClass().isAssignableFrom(Map.class)){ return convertValueMapToCompoundTag((Map<String, Object>)value); }
		if(value.getClass().equals(double.class)){ return nbtdc.newInstance(value); }
		if(value.getClass().equals(float.class)){ return nbtfc.newInstance(value); }
		if(value.getClass().equals(int.class)){ return nbtic.newInstance(value); }
		if(value.getClass().equals(int[].class)){ return nbtiac.newInstance(value); }
		if(value.getClass().isAssignableFrom(List.class)){ return convertValueListToListTag((List<Object>)value); }
		if(value.getClass().equals(long.class)){ return nbtloc.newInstance(value); }
		if(value.getClass().equals(short.class)){ return nbtsc.newInstance(value); }
		if(value.getClass().equals(String.class)){ return nbtstc.newInstance(value); }
		if(value.getClass().equals(long[].class)){ return nbtlac.newInstance(value); }
		return null;
	}
	
	@SuppressWarnings({ "unchecked" })
	public Map<String, Object> convertCompoundTagToValueMap(Object nbt) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> map = (Map<String, Object>)getMap(nbt);
		for(Entry<String, Object> e : map.entrySet()){
			Object nbti = e.getValue();
			Object data = getData(nbti);
			if(data != null){
				ret.put(e.getKey(), data);
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> convertListTagToValueList(Object nbttl) throws Exception{
		List<Object> ret = new ArrayList<Object>();
		List<Object> list = (List<Object>)nbtld.get(nbttl);
		for(Object e : list){
			Object data = getData(e);
			if(data != null){
				ret.add(data);
			}
		}
		return ret;
	}
	
	public Object convertValueMapToCompoundTag(Map<String, Object> map) throws Exception{
		Map<String, Object> value = new HashMap<String, Object>();
		for(Entry<String, Object> e : map.entrySet()){
			value.put(e.getKey(), createData(e.getValue()));
		}
		Object ret = getNewNBTTagCompound();
		nbttcm.set(ret, value);
		return ret;
	}
	
	public Object convertValueListToListTag(List<Object> list) throws Exception{
		List<Object> value = new ArrayList<Object>();
		for(Object e : list){
			value.add(createData(e));
		}
		Object ret = getNewNBTTagList();
		nbttcm.set(ret, value);
		if(value.size() > 0){
			nbtlt.set(ret, (byte)gti.invoke(value.get(0)));
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public void convertListTagToJSON(Object nbttl, JSONArray ja, JSONArray helper) throws Exception{
		List<Object> list = (List<Object>)nbtld.get(nbttl);
		for(Object e : list){
			Object data = getDataJSON(e, ja, helper);
			if(data != null){
				ja.put(data);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void convertListTagToJSON(Object nbttl, JSONArray ja) throws Exception{
		List<Object> list = (List<Object>)nbtld.get(nbttl);
		for(Object e : list){
			Object data = getDataJSON(e);
			if(data != null){
				ja.put(data);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public void convertCompoundTagToJSON(Object nbt, JSONObject jo, JSONObject helper) throws Exception{
		Map<String, Object> map = (Map<String, Object>)getMap(nbt);
		for(Entry<String, Object> e : map.entrySet()){
			Object nbti = e.getValue();
			Object data = getDataJSON(e.getKey(), nbti, jo, helper);
			if(data != null){
				jo.put(e.getKey(), data);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void convertCompoundTagToJSON(Object nbt, JSONObject jo) throws Exception{
		Map<String, Object> map = (Map<String, Object>)getMap(nbt);
		for(Entry<String, Object> e : map.entrySet()){
			Object nbti = e.getValue();
			Object data = getDataJSON(nbti);
			if(data != null){
				jo.put(e.getKey(), data);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public Object convertJSONToCompoundTag(JSONObject jo, JSONObject helper) throws Exception{
		Map<String, Object> value = new HashMap<String, Object>();
		Iterator<String> it = jo.keys();
		while(it.hasNext()){
			String e = it.next();
			value.put(e, createDataJSON(e, jo, helper));
		}
		Object ret = getNewNBTTagCompound();
		nbttcm.set(ret, value);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Object convertJSONToCompoundTag(JSONObject jo) throws Exception{
		Map<String, Object> value = new HashMap<String, Object>();
		Iterator<String> it = jo.keys();
		while(it.hasNext()){
			String e = it.next();
			value.put(e, createDataJSON(e, jo));
		}
		Object ret = getNewNBTTagCompound();
		nbttcm.set(ret, value);
		return ret;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Deprecated
	public Object convertJSONToListTag(JSONArray ja, JSONArray helper) throws Exception{
		List value = new ArrayList();
		for(int i = 0; i < ja.length(); i++){
			value.add(createDataJSON(i, ja, helper));
		}
		Object ret = getNewNBTTagList();
		nbtld.set(ret, value);
		if(value.size() > 0){
			nbtlt.set(ret, (byte)gti.invoke(value.get(0)));
		}
		return ret;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convertJSONToListTag(JSONArray ja) throws Exception{
		List value = new ArrayList();
		for(int i = 0; i < ja.length(); i++){
			value.add(createDataJSON(i, ja));
		}
		Object ret = getNewNBTTagList();
		nbtld.set(ret, value);
		if(value.size() > 0){
			nbtlt.set(ret, (byte)gti.invoke(value.get(0)));
		}
		return ret;
	}
	
	@Deprecated
	public Object getDataJSON(String key, Object nbt, JSONObject jo, JSONObject helper) throws Exception{
		int i = (int)((byte)gti.invoke(nbt));
		Object ret = null;
		Object help = i;
		switch(i){
		case NBTConstants.TYPE_BYTE:
			ret = nbtbd.get(nbt);
			break;
		case NBTConstants.TYPE_SHORT:
			ret = nbtsd.get(nbt);
			break;
		case NBTConstants.TYPE_INT:
			ret = nbtid.get(nbt);
			break;
		case NBTConstants.TYPE_LONG:
			ret = nbtlod.get(nbt);
			break;
		case NBTConstants.TYPE_FLOAT:
			ret = nbtfd.get(nbt);
			break;
		case NBTConstants.TYPE_DOUBLE:
			ret = nbtdd.get(nbt);
			break;
		case NBTConstants.TYPE_BYTE_ARRAY:
			ret = nbtbad.get(nbt);
			break;
		case NBTConstants.TYPE_STRING:
			ret = nbtstd.get(nbt);
			break;
		case NBTConstants.TYPE_LIST:{
			JSONArray ja1 = new JSONArray();
			JSONArray helper1 = new JSONArray();
			convertListTagToJSON(nbt, ja1, helper1);
			ret = ja1;
			help = helper1;
			break;
		}
		case NBTConstants.TYPE_COMPOUND:
			JSONObject jo1 = new JSONObject();
			JSONObject helper1 = new JSONObject();
			convertCompoundTagToJSON(nbt, jo1, helper1);
			ret = jo1;
			help = helper1;
			break;
		case NBTConstants.TYPE_INT_ARRAY:
			ret = nbtiad.get(nbt);
			break;
		case NBTConstants.TYPE_LONG_ARRAY:
			ret = nbtlad.get(nbt);
			break;
		}
		if(ret != null){
			helper.put(key, help);
		}
		return ret;
	}
	
	public JSONArray getDataJSON(Object nbt) throws Exception{
		int i = (int)((byte)gti.invoke(nbt));
		Object ret = null;
		switch(i){
		case NBTConstants.TYPE_BYTE:
			ret = nbtbd.get(nbt);
			break;
		case NBTConstants.TYPE_SHORT:
			ret = nbtsd.get(nbt);
			break;
		case NBTConstants.TYPE_INT:
			ret = nbtid.get(nbt);
			break;
		case NBTConstants.TYPE_LONG:
			ret = nbtlod.get(nbt);
			break;
		case NBTConstants.TYPE_FLOAT:
			ret = nbtfd.get(nbt);
			break;
		case NBTConstants.TYPE_DOUBLE:
			ret = nbtdd.get(nbt);
			break;
		case NBTConstants.TYPE_BYTE_ARRAY:
			ret = nbtbad.get(nbt);
			break;
		case NBTConstants.TYPE_STRING:
			ret = nbtstd.get(nbt);
			break;
		case NBTConstants.TYPE_LIST:{
			JSONArray ja1 = new JSONArray();
			convertListTagToJSON(nbt, ja1);
			ret = ja1;
			break;
		}
		case NBTConstants.TYPE_COMPOUND:
			JSONObject jo1 = new JSONObject();
			convertCompoundTagToJSON(nbt, jo1);
			ret = jo1;
			break;
		case NBTConstants.TYPE_INT_ARRAY:
			ret = nbtiad.get(nbt);
			break;
		case NBTConstants.TYPE_LONG_ARRAY:
			ret = nbtlad.get(nbt);
			break;
		}
		if(ret == null)
			return null;
		return new JSONArray(new Object[]{ i, ret });
	}
	
	@Deprecated
	public Object getDataJSON(Object nbt, JSONArray ja, JSONArray helper) throws Exception{
		int i = (int)((byte)gti.invoke(nbt));
		Object ret = null;
		Object help = i;
		switch(i){
		case NBTConstants.TYPE_BYTE:
			ret = nbtbd.get(nbt);
			break;
		case NBTConstants.TYPE_SHORT:
			ret = nbtsd.get(nbt);
			break;
		case NBTConstants.TYPE_INT:
			ret = nbtid.get(nbt);
			break;
		case NBTConstants.TYPE_LONG:
			ret = nbtlod.get(nbt);
			break;
		case NBTConstants.TYPE_FLOAT:
			ret = nbtfd.get(nbt);
			break;
		case NBTConstants.TYPE_DOUBLE:
			ret = nbtdd.get(nbt);
			break;
		case NBTConstants.TYPE_BYTE_ARRAY:
			ret = nbtbad.get(nbt);
			break;
		case NBTConstants.TYPE_STRING:
			ret = nbtstd.get(nbt);
			break;
		case NBTConstants.TYPE_LIST:{
			JSONArray ja1 = new JSONArray();
			JSONArray helper1 = new JSONArray();
			convertListTagToJSON(nbt, ja1, helper1);
			ret = ja1;
			break;
		}
		case NBTConstants.TYPE_COMPOUND:
			JSONObject jo1 = new JSONObject();
			JSONObject helper1 = new JSONObject();
			convertCompoundTagToJSON(nbt, jo1, helper1);
			ret = jo1;
			help = helper1;
			break;
		case NBTConstants.TYPE_INT_ARRAY:
			ret = nbtiad.get(nbt);
			break;
		case NBTConstants.TYPE_LONG_ARRAY:
			ret = nbtlad.get(nbt);
			break;
		}
		if(ret != null){
			helper.put(help);
		}
		return ret;
	}
	
	@Deprecated
	public Object createDataJSON(String key, JSONObject jo, JSONObject helper) throws Exception{
		Object help = helper.get(key);
		Object ret = null;
		if(help instanceof JSONObject){
			JSONObject jo1 = jo.getJSONObject(key);
			JSONObject helper1 = (JSONObject)help;
			ret = convertJSONToCompoundTag(jo1, helper1);
		}else if(help instanceof JSONArray){
			JSONArray ja1 = jo.getJSONArray(key);
			JSONArray helper1 = (JSONArray)help;
			ret = convertJSONToListTag(ja1, helper1);
		}else{
			int i = (int)help;
			switch(i){
			case NBTConstants.TYPE_BYTE:
				ret = nbtbc.newInstance(getByte(jo.get(key)));
				break;
			case NBTConstants.TYPE_SHORT:
				ret = nbtsc.newInstance(getShort(jo.get(key)));
				break;
			case NBTConstants.TYPE_INT:
				ret = nbtic.newInstance(getInt(jo.get(key)));
				break;
			case NBTConstants.TYPE_LONG:
				ret = nbtloc.newInstance(getLong(jo.get(key)));
				break;
			case NBTConstants.TYPE_FLOAT:
				ret = nbtfc.newInstance(getFloat(jo.get(key)));
				break;
			case NBTConstants.TYPE_DOUBLE:
				ret = nbtdc.newInstance(getDouble(jo.get(key)));
				break;
			case NBTConstants.TYPE_BYTE_ARRAY:{
				JSONArray ja = jo.getJSONArray(key);
				byte[] b = new byte[ja.length()];
				for(int a = 0; a < ja.length(); a++){
					b[a] = getByte(ja.get(a));
				}
				return nbtbac.newInstance(b);
			}
			case NBTConstants.TYPE_STRING:
				ret = nbtstc.newInstance((String)jo.get(key));
				break;
			case NBTConstants.TYPE_INT_ARRAY: {
				JSONArray ja = jo.getJSONArray(key);
				int[] b = new int[ja.length()];
				for (int a = 0; a < ja.length(); a++) {
					b[a] = getInt(ja.get(a));
				}
				return nbtiac.newInstance(b);
			}
			case NBTConstants.TYPE_LONG_ARRAY: {
				JSONArray ja = jo.getJSONArray(key);
				long[] b = new long[ja.length()];
				for (int a = 0; a < ja.length(); a++) {
					b[a] = getLong(ja.get(a));
				}
				return nbtlac.newInstance(b);
			}
			}
		}
		return ret;
	}
	
	public Object createDataJSON(String key, JSONObject jo) throws Exception{
		JSONArray j = jo.getJSONArray(key);
		Object ret = null;
		int i = (int)j.getInt(0);
		switch(i){
		case NBTConstants.TYPE_COMPOUND:
			ret = convertJSONToCompoundTag(j.getJSONObject(1));
			break;
		case NBTConstants.TYPE_BYTE:
			ret = nbtbc.newInstance(getByte(j.get(1)));
			break;
		case NBTConstants.TYPE_SHORT:
			ret = nbtsc.newInstance(getShort(j.get(1)));
			break;
		case NBTConstants.TYPE_INT:
			ret = nbtic.newInstance(getInt(j.get(1)));
			break;
		case NBTConstants.TYPE_LONG:
			ret = nbtloc.newInstance(getLong(j.get(1)));
			break;
		case NBTConstants.TYPE_FLOAT:
			ret = nbtfc.newInstance(getFloat(j.get(1)));
			break;
		case NBTConstants.TYPE_DOUBLE:
			ret = nbtdc.newInstance(getDouble(j.get(1)));
			break;
		case NBTConstants.TYPE_BYTE_ARRAY:{
			JSONArray ja = j.getJSONArray(1);
			byte[] b = new byte[ja.length()];
			for(int a = 0; a < ja.length(); a++){
				b[a] = getByte(ja.get(a));
			}
			return nbtbac.newInstance(b);
		}
		case NBTConstants.TYPE_STRING:
			ret = nbtstc.newInstance((String)j.get(1));
			break;
		case NBTConstants.TYPE_INT_ARRAY: {
			JSONArray ja = j.getJSONArray(1);
			int[] b = new int[ja.length()];
			for (int a = 0; a < ja.length(); a++) {
				b[a] = getInt(ja.get(a));
			}
			return nbtiac.newInstance(b);
		}
		case NBTConstants.TYPE_LONG_ARRAY: {
			JSONArray ja = j.getJSONArray(1);
			long[] b = new long[ja.length()];
			for (int a = 0; a < ja.length(); a++) {
				b[a] = getLong(ja.get(a));
			}
			return nbtlac.newInstance(b);
		}
		case NBTConstants.TYPE_LIST:
			ret = convertJSONToListTag(j.getJSONArray(1));
			break;
		}
		return ret;
	}
	
	public byte getByte(Object o){
		if(o.getClass().equals(Integer.class)){ return (byte)(int)o; }
		if(o.getClass().equals(Short.class)){ return (byte)(short)o; }
		if(o.getClass().equals(Integer.class)){ return (byte)(int)o; }
		return (byte)o;
	}
	
	public short getShort(Object o){
		if(o.getClass().equals(Integer.class)){ return (short)(int)o; }
		if(o.getClass().equals(Byte.class)){ return (short)(byte)o; }
		if(o.getClass().equals(Integer.class)){ return (short)(int)o; }
		return (short)o;
	}
	
	public int getInt(Object o){
		if(o.getClass().equals(Short.class)){ return (int)(short)o; }
		if(o.getClass().equals(Byte.class)){ return (int)(byte)o; }
		return (int)o;
	}
	
	public double getDouble(Object o){
		if(o.getClass().equals(Float.class)){ return (double)(float)o; }
		if(o.getClass().equals(Long.class)){ return (double)(long)o; }
		if(o.getClass().equals(Integer.class)){ return (double)(int)o; }
		return (double)o;
	}
	
	public float getFloat(Object o){
		if(o.getClass().equals(Double.class)){ return (float)(double)o; }
		if(o.getClass().equals(Long.class)){ return (float)(long)o; }
		if(o.getClass().equals(Integer.class)){ return (float)(int)o; }
		return (float)o;
	}
	
	public long getLong(Object o){
		if(o.getClass().equals(Float.class)){ return (long)(float)o; }
		if(o.getClass().equals(Double.class)){ return (long)(double)o; }
		if(o.getClass().equals(Integer.class)){ return (long)(int)o; }
		return (long)o;
	}
	
	@Deprecated
	public Object createDataJSON(int key, JSONArray jo, JSONArray helper) throws Exception{
		Object help = helper.get(key);
		Object ret = null;
		if(help instanceof JSONObject){
			JSONObject jo1 = jo.getJSONObject(key);
			JSONObject helper1 = (JSONObject)help;
			return convertJSONToCompoundTag(jo1, helper1);
		}else if(help instanceof JSONArray){
			JSONArray ja1 = jo.getJSONArray(key);
			JSONArray helper1 = (JSONArray)help;
			return convertJSONToListTag(ja1, helper1);
		}else{
			int i = (int)help;
			switch(i){
			case NBTConstants.TYPE_BYTE:
				ret = nbtbc.newInstance(getByte(jo.get(key)));
				break;
			case NBTConstants.TYPE_SHORT:
				ret = nbtsc.newInstance(getShort(jo.get(key)));
				break;
			case NBTConstants.TYPE_INT:
				ret = nbtic.newInstance(getInt(jo.get(key)));
				break;
			case NBTConstants.TYPE_LONG:
				ret = nbtloc.newInstance(getLong(jo.get(key)));
				break;
			case NBTConstants.TYPE_FLOAT:
				ret = nbtfc.newInstance(getFloat(jo.get(key)));
				break;
			case NBTConstants.TYPE_DOUBLE:
				ret = nbtdc.newInstance(getDouble(jo.get(key)));
				break;
			case NBTConstants.TYPE_BYTE_ARRAY:{
				JSONArray ja = jo.getJSONArray(key);
				byte[] b = new byte[ja.length()];
				for(int a = 0; a < ja.length(); a++){
					b[a] = getByte(ja.get(a));
				}
				return nbtbac.newInstance(b);
			}
			case NBTConstants.TYPE_STRING:
				ret = nbtstc.newInstance((String)jo.get(key));
				break;
			case NBTConstants.TYPE_INT_ARRAY: {
				JSONArray ja = jo.getJSONArray(key);
				int[] b = new int[ja.length()];
				for (int a = 0; a < ja.length(); a++) {
					b[a] = getInt(ja.get(a));
				}
				return nbtiac.newInstance(b);
			}
			case NBTConstants.TYPE_LONG_ARRAY: {
				JSONArray ja = jo.getJSONArray(key);
				long[] b = new long[ja.length()];
				for (int a = 0; a < ja.length(); a++) {
					b[a] = getLong(ja.get(a));
				}
				return nbtlac.newInstance(b);
			}
			}
		}
		return ret;
	}
	
	public Object createDataJSON(int key, JSONArray jo) throws Exception{
		JSONArray j = jo.getJSONArray(key);
		Object ret = null;
		int i = (int)j.getInt(0);
		switch(i){
		case NBTConstants.TYPE_COMPOUND:
			ret = convertJSONToCompoundTag(j.getJSONObject(1));
			break;
		case NBTConstants.TYPE_BYTE:
			ret = nbtbc.newInstance(getByte(j.get(1)));
			break;
		case NBTConstants.TYPE_SHORT:
			ret = nbtsc.newInstance(getShort(j.get(1)));
			break;
		case NBTConstants.TYPE_INT:
			ret = nbtic.newInstance(getInt(j.get(1)));
			break;
		case NBTConstants.TYPE_LONG:
			ret = nbtloc.newInstance(getLong(j.get(1)));
			break;
		case NBTConstants.TYPE_FLOAT:
			ret = nbtfc.newInstance(getFloat(j.get(1)));
			break;
		case NBTConstants.TYPE_DOUBLE:
			ret = nbtdc.newInstance(getDouble(j.get(1)));
			break;
		case NBTConstants.TYPE_BYTE_ARRAY:{
			JSONArray ja = j.getJSONArray(1);
			byte[] b = new byte[ja.length()];
			for(int a = 0; a < ja.length(); a++){
				b[a] = getByte(ja.get(a));
			}
			return nbtbac.newInstance(b);
		}
		case NBTConstants.TYPE_STRING:
			ret = nbtstc.newInstance((String)j.get(1));
			break;
		case NBTConstants.TYPE_INT_ARRAY: {
			JSONArray ja = jo.getJSONArray(key);
			int[] b = new int[ja.length()];
			for (int a = 0; a < ja.length(); a++) {
				b[a] = getInt(ja.get(a));
			}
			return nbtiac.newInstance(b);
		}
		case NBTConstants.TYPE_LONG_ARRAY: {
			JSONArray ja = jo.getJSONArray(key);
			long[] b = new long[ja.length()];
			for (int a = 0; a < ja.length(); a++) {
				b[a] = getLong(ja.get(a));
			}
			return nbtlac.newInstance(b);
		}
		case NBTConstants.TYPE_LIST:
			ret = convertJSONToListTag(j.getJSONArray(1));
			break;
		}
		return ret;
	}
	
	public boolean compareBaseTag(Object tag, Object tag1) throws Exception{
		int i = (int)((byte)gti.invoke(tag));
		int i1 = (int)((byte)gti.invoke(tag1));
		if(i != i1)
			return false;
		switch(i){
		case NBTConstants.TYPE_BYTE:
			Byte b = (byte)nbtbd.get(tag);
			Byte b1 = (byte)nbtbd.get(tag1);
			return b.equals(b1);
		case NBTConstants.TYPE_SHORT:
			Short s = (short)nbtsd.get(tag);
			Short s1 = (short)nbtsd.get(tag1);
			return s.equals(s1);
		case NBTConstants.TYPE_INT:
			Integer a = (int)nbtid.get(tag);
			Integer a1 = (int)nbtid.get(tag1);
			return a.equals(a1);
		case NBTConstants.TYPE_LONG:
			Long l = (long)nbtlod.get(tag);
			Long l1 = (long)nbtlod.get(tag1);
			return l.equals(l1);
		case NBTConstants.TYPE_FLOAT:
			Float f = (float)nbtfd.get(tag);
			Float f1 = (float)nbtfd.get(tag1);
			return f.equals(f1);
		case NBTConstants.TYPE_DOUBLE:
			Double d = (double)nbtdd.get(tag);
			Double d1 = (double)nbtdd.get(tag1);
			return d.equals(d1);
		case NBTConstants.TYPE_BYTE_ARRAY:
			byte[] ba = (byte[])nbtbad.get(tag);
			byte[] ba1 = (byte[])nbtbad.get(tag1);
			return ba.equals(ba1);
		case NBTConstants.TYPE_STRING:
			String st = (String)nbtstd.get(tag);
			String st1 = (String)nbtstd.get(tag1);
			return st.equals(st1);
		case NBTConstants.TYPE_LIST:{
			return compareListTag(tag, tag1);
		}
		case NBTConstants.TYPE_COMPOUND:
			return compareCompoundTag(tag, tag1);
		case NBTConstants.TYPE_INT_ARRAY: {
			int[] ia = (int[]) nbtiad.get(tag);
			int[] ia1 = (int[]) nbtiad.get(tag);
			return ia.equals(ia1);
		}
		case NBTConstants.TYPE_LONG_ARRAY: {
			long[] ia = (long[]) nbtlad.get(tag);
			long[] ia1 = (long[]) nbtlad.get(tag);
			return ia.equals(ia1);
		}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean compareCompoundTag(Object tag, Object tag1) throws Exception{
		Map<String, Object> map = (Map<String, Object>)getMap(tag);
		Map<String, Object> map1 = (Map<String, Object>)getMap(tag1);
		if(map.size() != map1.size())
			return false;
		if(!map.keySet().containsAll(map1.keySet()))
			return false;
		for(Entry<String, Object> e : map.entrySet()){
			Object o = e.getValue();
			Object o1 = map1.get(e.getKey());
			if(!compareBaseTag(o, o1))
				return false;
		}
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean compareListTag(Object tag, Object tag1) throws Exception{
		List list = (List)nbtld.get(tag);
		List list1 = (List)nbtld.get(tag1);
		if(list.size() != list1.size())
			return false;
		if(list.isEmpty() && list1.isEmpty())
			return true;
		List copy = new ArrayList(list);
		List copy1 = new ArrayList(list1);
		Iterator it = copy.iterator();
		while(it.hasNext()){
			Object o = it.next();
			Iterator it1 = copy1.iterator();
			boolean cont = false;
			while(it1.hasNext()){
				Object o1 = it1.next();
				if(compareBaseTag(o, o1)){
					it1.remove();
					it.remove();
					cont = true;
					break;
				}
				;
			}
			if(!cont)
				return false;
		}
		return copy.isEmpty() && copy1.isEmpty();
	}
	
	public boolean compare(ItemStack is1, ItemStack is2){
		if(is1.getType().equals(is2.getType())){
			if(is1.getDurability() == is2.getDurability()){
				try{
					Object nis1 = getNMSCopy(is1);
					Object nis2 = getNMSCopy(is2);
					Object tis1 = getTag(nis1);
					Object tis2 = getTag(nis2);
					if(tis1 != null && tis2 == null){
						if(isEmpty(tis1))
							return true;
						return false;
					}
					if(tis1 == null && tis2 != null){
						if(isEmpty(tis2))
							return true;
						return false;
					}
					if(tis1 == null && tis2 == null){ return true; }
					return compareCompoundTag(tis1, tis2);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public boolean canMerge(ItemStack add, ItemStack to){
		return compare(add, to);
	}
	
	public boolean isModified(ItemStack is){
		ItemStack is1 = is.clone();
		is1.setAmount(1);
		ItemStack is2 = new ItemStack(is.getType(), 1, is.getDurability());
		return !is1.equals(is2);
	}
	
	public void sortByMaterial(List<ItemStack> items){
		Collections.sort(items, new MaterialComparator());
	}
	
	public class MaterialComparator implements Comparator<ItemStack>{
		@Override
		public int compare(ItemStack arg0, ItemStack arg1){
			return arg0.getType().name().compareTo(arg1.getType().name());
		}
	}
	
	public void sortByName(List<ItemStack> items){
		Collections.sort(items, new NameComparator());
	}
	
	public class NameComparator implements Comparator<ItemStack>{
		@Override
		public int compare(ItemStack arg0, ItemStack arg1){
			int i = 0;
			try{
				i = ChatColor.stripColor(getName(arg0)).compareTo(ChatColor.stripColor(getName(arg1)));
			}catch(Exception e){
				e.printStackTrace();
			}
			return i;
		}
	}
	
	public void sortByAmount(List<ItemStack> items){
		Collections.sort(items, new AmountComparator());
	}
	
	public class AmountComparator implements Comparator<ItemStack>{
		@Override
		public int compare(ItemStack arg0, ItemStack arg1){
			int i = arg1.getAmount() - arg0.getAmount();
			if(i == 0){
				try{
					i = ChatColor.stripColor(getName(arg0)).compareTo(ChatColor.stripColor(getName(arg1)));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return i;
		}
	}
	
	public ItemStack convertJSONToItemStack(JSONObject jo) throws Exception{
		Material material;
		try{
			material = Material.valueOf(jo.getString("material"));
		}catch(Exception e){
			material = Material.valueOf("LEGACY_" + jo.getString("material"));
		}
		int amount = jo.getInt("amount");
		int durability = jo.getInt("durability");
		ItemStack is = new ItemStack(material, amount);
		if(Version.isAtleastThirteen() && material.isLegacy()){
			Object nmis = ItemUtils.getInstance().getNMSCopy(is);
			is = ItemUtils.getInstance().asBukkitCopy(nmis);
		}
		Object nmis = getNMSCopy(is);
		JSONObject jo1 = jo.getJSONObject("tag");
		if(jo1.length() == 0){
			is = asBukkitCopy(nmis);
			is.setDurability((short)durability);
			return is;
		}
		
		if(Version.isAtleastThirteen()){
			IItemUtils.fixTags(jo1);
		}
		Object tag = convertJSONToCompoundTag(jo1);
		
		setTag(nmis, tag);
		is = asBukkitCopy(nmis);
		is.setDurability((short)durability);
		
		return is;
	}

	
	public JSONObject convertItemStackToJSON(ItemStack is) throws Exception{
		JSONObject jo = new JSONObject();
		jo.put("material", is.getType().name());
		jo.put("amount", is.getAmount());
		jo.put("durability", is.getDurability());
		JSONObject jo2 = new JSONObject();
		Object nmis = getNMSCopy(is);
		Object tag = getTag(nmis);
		if(tag != null){
			convertCompoundTagToJSON(tag, jo2);
		}
		jo.put("tag", jo2);
		return jo;
	}
}
