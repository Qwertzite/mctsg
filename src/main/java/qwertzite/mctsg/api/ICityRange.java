package qwertzite.mctsg.api;

public interface ICityRange {
	public EnumFillPolicy getPolicy();
	public boolean isAreaQualified(BuildArea area);
}
