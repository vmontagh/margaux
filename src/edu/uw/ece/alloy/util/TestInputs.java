package edu.uw.ece.alloy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class TestInputs {
	public static Collection<Object[]> generatorAlloy() {
		return oneD2twoD(Utils.filesR("models/tests/gen/", "^.*\\.als$"));
	}


	public static Collection<Object[]> oneD2twoD(final Object[] in) {
		final Collection<Object[]> out = new ArrayList<Object[]>(in.length);
		for (final Object obj : in) {
			out.add(new Object[] {obj});
		}
		return out;
	}

	private static Map<String,Set<String>> freeze(final Map<String,Set<String>> m) {
		final Map<String,Set<String>> n = new TreeMap<String,Set<String>>();
		for (final Map.Entry<String, Set<String>> e : m.entrySet()) {
			final Set<String> s = new TreeSet<String>(e.getValue());
			n.put(e.getKey(), s);
		}
		return Collections.unmodifiableMap(n);
	}

	private static void madd(final Map<String, Set<String>> m, final String s, final Set<String> ss) {
		final Set<String> existingSet = m.get(s);
		if (existingSet == null) {
			m.put(s, ss);
		} else {
			existingSet.addAll(ss);
		}
	}

	private static void invert(final Map<String, Set<String>> m) {
		final Map<String,Set<String>> toAdd = new TreeMap<String,Set<String>>();

		// make sure the inverse keys exist
		for (final Map.Entry<String, Set<String>> e : m.entrySet()) {
			final String k = e.getKey();
			final Set<String> s = e.getValue();
			for (final String k2 : s) {
				if (!m.containsKey(k2)) {
					toAdd.put(k2, s(k));
				}
			}
		}
		m.putAll(toAdd);

		// make sure the inverse sets are complete
		for (final Map.Entry<String, Set<String>> e : m.entrySet()) {
			final String k = e.getKey();
			final Set<String> s = e.getValue();
			for (final String k2 : s) {
				final Set<String> s2 = m.get(k2);
				s2.add(k);
				s2.addAll(s);
			}
		}
	}

	private static Set<String> s(final String...args) {
		final Set<String> s = new TreeSet<String>();
		for (final String a : args) {
			s.add(a);
		}
		return s;
	}

}
