package gamelauncher.engine.util.property;

import java.util.function.Supplier;

import de.dasbabypixel.api.property.ReadOnlyStorage;

@SuppressWarnings("javadoc")
public class SupplierReadOnlyStorage<T> implements ReadOnlyStorage<T> {

	private final Supplier<T> supplier;

	public SupplierReadOnlyStorage(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public T read() {
		return supplier.get();
	}

}