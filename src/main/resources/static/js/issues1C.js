const table = document.getElementById('mytable');

//行を追加するボタンにイベントリスナーを設定
document.getElementById('addRow').addEventListener('click', function() {
	//新しい行を作成
	const newRow = table.insertRow(-1);

	// 新しい行にセルを追加
	const cell1 = newRow.insertCell(-1);
	const cell2 = newRow.insertCell(-1);

	cell1.textContent = 'Number' + (table.rows.length -1);
	cell2.textContent = 'Name' + (table.rows.length -1);
});

// 行を削除するボタンにイベントリスナーを設定
document.getElementById('deleteRow').addEventListener('click', function() {
	// 最後の行を削除
	const deleteRow = table.rows.length;

	if (deleteRow > 1) {
		table.deleteRow(table.rows.length - 1);
	}
});