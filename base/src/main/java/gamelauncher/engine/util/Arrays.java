package gamelauncher.engine.util;

import java.util.ArrayList;
import java.util.List;

public class Arrays {

	public static <T> List<T> asList(T[] array) {
		List<T> list = new ArrayList<>(array.length);
		for (int i = 0; i < array.length; i++) {
			list.add(i, array[i]);
		}
		return list;
	}
}