package com.yuanhonglong.util;

import java.util.Collection;

/**
 * 这是一个列表和队列的混合数据结构,基本上是一个队列,但是与队列不同,它允许你访问任何一个元素
 *
 * @author 天命剑主 <br>
 *         create by eclipse<br>
 *         on 2015年11月14日 <br>
 */
public class ListQueue<E> {

	private Object[]	datas;	// 元素,有意义的元素 [head,tail)
	private int			head;	// 队首
	private int			tail;	// 队尾

	@SuppressWarnings("unused")
	public ListQueue() {
		this(200);
	}

	public ListQueue(int capacity) {
		datas = new Object[capacity + 1];
		head = 0;
		tail = 0;
	}

	/**
	 * 在末尾添加一个元素
	 *
	 * @return 如果队列满返回false
	 */
	public boolean add(E e) {
		if (isFull()) {
			return false;
		} else {
			datas[tail] = e;
			tail = (tail + 1) % datas.length;
			return true;
		}
	}

	@SuppressWarnings("unused")
	/**
	 * 将集合中的所有元素添加到队列中
	 *
	 * @param collection
	 *            集合
	 * @return 如果装不下返回false
	 */
	public boolean addAll(Collection<? extends E> collection) {
		for (E e : collection) {
			if (!add(e)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unused")
	/**
	 * 在头部添加一个元素
	 *
	 * @return 如果队列满返回false
	 */
	public boolean addHead(E e) {
		if (isFull()) {
			return false;
		} else {
			head = ((head + datas.length) - 1) % datas.length;
			datas[head] = e;
			return true;
		}
	}

	/**
	 * 在末尾添加一个元素
	 *
	 * @return 如果队列满返回false
	 */
	public boolean addTail(E e) {
		return add(e);
	}

	@SuppressWarnings("unused")
	/**
	 * @return 队列容量
	 */
	public int capacity() {
		return datas.length - 1;
	}

	/**
	 * 移除所有元素
	 */
	public void clear() {
		tail = head;
	}

	@SuppressWarnings("unused")
	/**
	 * @param obj
	 *            查询对象
	 * @return 查询队列中是否存在某个元素
	 */
	public boolean contains(Object obj) {
		for (int i = head; i != tail; i = (i + 1) % datas.length) {
			Object object = datas[i];
			if (object.equals(obj)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 截断
	 * 
	 * @param size
	 *            新的大小
	 * @return 是否成功截断
	 */
	public boolean cut(int size) {
		if ((size < 0) || (size > size())) {
			return false;
		}
		tail = (head + size) % datas.length;
		return true;
	}

	@SuppressWarnings("unchecked")
	/**
	 * 类似于get(0),但是不做空检查
	 */
	public E element() {
		return isEmpty() ? null : (E) datas[head];
	}

	@SuppressWarnings("unchecked")
	/**
	 * 返回元素
	 *
	 * @param index
	 *            索引
	 * @return 元素,如果队列为空或者索引不正确返回null
	 */
	public E get(int index) {
		if ((index < 0) || (index >= size())) {
			return null;
		} else {
			return (E) datas[(head + index) % datas.length];
		}
	}

	/**
	 * @return 返回队列是否为空
	 */
	public boolean isEmpty() {
		return tail == head;
	}

	/**
	 * @return 返回队列是否为满
	 */
	public boolean isFull() {
		return (((head + datas.length) - tail) % datas.length) == 1;
	}

	@SuppressWarnings("unused")
	/**
	 * 类似于get(0),但是不做空检查
	 */
	public E peek() {
		return element();
	}

	@SuppressWarnings("unused")
	/**
	 * 移除队尾元素
	 *
	 * @return 如果队列为空返回false
	 */
	public boolean remove() {
		return removeTail();
	}

	/**
	 * 移除队首元素
	 *
	 * @return 如果队列为空返回false
	 */
	public boolean removeHead() {
		if (!isEmpty()) {
			head = (head + 1) % datas.length;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 移除队尾元素
	 *
	 * @return 如果队列为空返回false
	 */
	public boolean removeTail() {
		if (!isEmpty()) {
			tail = ((tail + datas.length) - 1) % datas.length;
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unused")
	/**
	 * 修改元素
	 *
	 * @param index
	 *            索引
	 * @param e
	 *            新元素
	 * @return 如果队列为空或者索引错误返回false
	 */
	public boolean set(int index, E e) {
		if ((index < 0) || (index >= size())) {
			return false;
		} else {
			datas[(head + index) % datas.length] = e;
			return true;
		}
	}

	/**
	 * 设置队列容量
	 *
	 * @param capacity
	 *            容量
	 * @return 如果容量为负数返回false
	 */
	public boolean setCapacity(int capacity) {
		if (capacity < 0) {
			return false;
		} else {
			datas = new Object[capacity + 1];
			head = 0;
			tail = 0;
			return true;
		}
	}

	/**
	 * @return 队列元素个数
	 */
	public int size() {
		if (tail < head) {
			return (tail + datas.length) - head;
		} else {
			return tail - head;
		}
	}

}
