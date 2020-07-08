//页面加载完以后调用
$(function () {
    $("#topBtn").click(setTop);
    $("#wBtn").click(setWonderful);
    $("#deleteBtn").click(deletePost);
})

function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        "/like",
        {
            "entityType":entityType,
            "entityId":entityId,
            "entityUserId":entityUserId,
            "postId":postId
        },
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg);
            }
        }
    )
}
//置顶
function setTop() {
    $.post(
      "/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                $("#topBtn").attr("disabled","disabled");
                $("#topBtn").val("已置顶")
                alert(data.msg);
            }else {
                alert(data.msg);
            }
        }
    );
}

//加精
function setWonderful() {
    $.post(
        "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                $("#wBtn").attr("disabled","disabled");
                $("#wBtn").val("加精")
                alert(data.msg);
            }else {
                alert(data.msg);
            }
        }
    );
}

//删除
function deletePost() {
    if(confirm('确实要删除该帖子吗?')){
        $.post(
            "/discuss/delete",
            {"id":$("#postId").val()},
            function (data) {
                data=$.parseJSON(data);
                if(data.code==0){
                    window.location.href="/";
                }else {
                    alert(data.msg);
                }
            }
        );
    }


}



