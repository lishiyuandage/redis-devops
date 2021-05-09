package com.bigbrotherlee.redis.admin.domain;

import java.util.List;

public class PageVO<T> {
	private int pages;
	private int total;
	private List<T> list;
	private int page;
	
	public PageVO(){
		this.page = 1;
		this.pages = 0;
		this.total = 0;
	}
	
	public int getPages() {
		return pages;
	}
	public void setPages(int pages) {
		this.pages = pages;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		
		this.total = total;
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
}
