$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//发送请求前，将csrf令牌设置到请求的消息头中
	/*var token=$("meta[name='_csrf']").attr("content")
	var header=$("meta[name='_csrf_header']").attr("content")
	$(document).ajaxSend(function (e,xhr,options) {
		xhr.setRequestHeader(header,token)
	})*/
	//获取标题和内容
	var title=$("#recipient-name").val();
	var content=$("#message-text").val();

	//发送异步请求
	$.post(
		"/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data=$.parseJSON(data);
			//设置消息
			$("#hintBody").text(data.msg)

			//显示消息
			$("#hintModal").modal("show");
			//延时一秒后关闭
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code==0){
					window.location.reload()
				}
			}, 1000);
		}
	)


}