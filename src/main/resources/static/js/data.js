//页面加载完以后调用
$(function () {
    $("#uvBtn").click(getUV);
    $("#dauBtn").click(getDAU);
})

//获取uv
function getUV() {
    var start=$("#startUVTxt").val()
    var end=$("#endUVTxt").val()
    if(start==""||end==""){
        alert("请先选择日期")
    }else {
        $.post(
            "/data/uv",
            {
                "start":start,
                "end":end
            },
            function (data) {
                data=$.parseJSON(data);
                if(data.code==0){
                    $("#uv_span").text(data.uvResult)
                }else {
                    alert(data.msg);
                }
            }
        );
    }

}

//获取uv
function getDAU() {
    var start=$("#startDAUTxt").val()
    var end=$("#endDAUTxt").val()
    if(start==""||end==""){
        alert("请先选择日期")
    }else {
        $.post(
            "/data/dau",
            {
                "start":start,
                "end":end
            },
            function (data) {
                data=$.parseJSON(data);
                if(data.code==0){
                    $("#dau_span").text(data.dauResult)
                }else {
                    alert(data.msg);
                }
            }
        );
    }

}
