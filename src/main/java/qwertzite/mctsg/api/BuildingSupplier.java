package qwertzite.mctsg.api;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BuildingSupplier {
	private Random rand;
	private List<Entry> entries;
	private int weightSum;
	
	private Entry next;
	
	public BuildingSupplier(Collection<IBuildingEntry> in, Random rand) {
		this.entries = new LinkedList<>();
		for (IBuildingEntry e : in) {
			if (!e.isEnabled()) { continue; }
			this.weightSum += e.getWeight();
			this.entries.add(new Entry(e));
		}
		this.rand = rand;
		this.setNext();
	}
	
	/**
	 * Re-selects the next building.
	 */
	public void setNext() {
		this.next = null;
		if (this.weightSum > 0) {
			int indx = this.rand.nextInt(this.weightSum);
			for (Entry e : this.entries) {
				if (e.entry.getWeight() > indx) {
					this.next = e;
					break;
				} else {
					indx -= e.entry.getWeight();
				}
			}
		}
	}
	
	public boolean hasNext() {
		return this.next != null;
	}
	
	public int getNextWidth() {
		return this.next.entry.getWidth();
	}
	
	public int getNextLength() {
		return this.next.entry.getLength();
	}
	
	public int getNextHeight() {
		return this.next.entry.getHeight();
	}
	
	public int getNextDepth() {
		return this.next.entry.getDepth();
	}
	
	public IBuildingEntry getNext() {
		return this.next.entry;
	}
	
	/**
	 * Marks the current building used and selects next building.
	 */
	public void markAsUsed() {
		this.next.incrementCount();
		if (!this.next.isValid()) {
			this.entries.remove(this.next);
			this.weightSum -= this.next.getWeight();
		}
		this.setNext();
	}
	
	private static class Entry {
		private IBuildingEntry entry;
		private int count;
		
		public Entry(IBuildingEntry entry) {
			this.entry = entry;
			this.count = 0;
		}
		
		public void incrementCount() {
			this.count++;
		}
		
		public boolean isValid() {
			return !this.entry.hasLimit() || this.count < this.entry.getLimit();
		}
		
		public int getWeight() {
			return this.entry.getWeight();
		}
	}
}
