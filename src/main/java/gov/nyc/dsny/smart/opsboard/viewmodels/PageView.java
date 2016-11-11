package gov.nyc.dsny.smart.opsboard.viewmodels;

import java.io.Serializable;
import java.util.List;

public class PageView implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<? extends Object> items;

	private long count;

	private long page;

	public long getCount() {
		return count;
	}

	public List<? extends Object> getItems() {
		return items;
	}

	public long getPage() {
		return page;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void setItems(List<? extends Object> items) {
		this.items = items;
	}

	public void setPage(long page) {
		this.page = page;
	}

}
