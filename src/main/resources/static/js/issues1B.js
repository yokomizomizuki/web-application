
document.getElementById('btn-name').addEventListener('click', function(e) {
	// 再読み込み防止
	e.preventDefault();

	// 入力フォームの内容を取得
	const name = document.getElementById('name').value;
	// 入力内容を画面に出力
	document.getElementById('input_name').textContent = `氏名：${name}`;
})

document.getElementById('btn-comment').addEventListener('click', function(e) {
	// 再読み込み防止
	e.preventDefault();

	// 入力フォームの内容を取得
	const comment = document.getElementById('comment').value;

//	// 入力内容を画面に出力
	document.getElementById('input_comment').textContent = `コメント：${comment}`;
})

document.getElementById('btn-gender').addEventListener('click', function(e) {
	// 再読み込み防止
	e.preventDefault();

	// 入力フォームの内容を取得
	const sex = form.querySelector('input[name="gender"]:checked')?.value || '未選択';
	// 入力内容を画面に出力
	document.getElementById('input_sex').textContent = `性別：${sex}`;
})

document.getElementById('btn-payments').addEventListener('click', function(e) {
	//再読み込み防止
	e.preventDefault();

	//入力フォームの内容を取得
	const pays = Array.from(form.querySelectorAll('input[name="payments"]:checked'))
		.map(el => el.value);
	//入力内容を画面に出力
	document.getElementById('input_pay').textContent = `利用する支払方法：${pays.length ? pays.join(' / ') : '未選択'}`;
})

document.getElementById('btn-season').addEventListener('click', function(e) {
	//再読み込み防止
	e.preventDefault();

	//入力フォームの内容を取得
	const season = document.getElementById('season').value;
	//入力内容を画面に出力
	document.getElementById('input_season').textContent = `好きな季節：${season}`;
})
