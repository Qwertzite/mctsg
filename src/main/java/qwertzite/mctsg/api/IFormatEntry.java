package qwertzite.mctsg.api;

import java.io.File;

public interface IFormatEntry<B extends IBuildingEntry> {

	/**
	 * ファイルフォーマットが適正かを調べる．まあsuffixだけでなく中身まで調べても問題はない．
	 * @param file
	 * @return
	 */
	public boolean checkSuffix(File file);
	
	/**
	 * 	指定されたファイルから新しい建物を返す．
	 * @param file 読み込み対象のファイル
	 * @return 読み込みに失敗したらnullを返す
	 * @throws null 失敗したら例外ではなくnullを返すように
	 */
	public B loadFromFile(File file);
}
