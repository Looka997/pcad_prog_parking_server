package server.model;

import java.util.Collection;

public interface Dao<T> {
    boolean insert(T t);
    boolean createTable();
    void load();
	T get(String id);
	Collection<T> getAll();
	boolean update(T t, String[] params);
	boolean delete(T t);
}
