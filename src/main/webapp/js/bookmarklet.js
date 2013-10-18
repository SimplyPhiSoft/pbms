var serverURL = "http://localhost:9090"

function myBookmarklet() {

    var bm = document.getElementById("bmContainer");
    if (bm === null || typeof bm === "undefined") {
        bm = document.createElement("div");
        bm.setAttribute("id", "bmContainer");
        bm.setAttribute("style", "width:380px; height:420px; position:fixed; top:0px; right:20px; z-index:9000000000; opacity:1; box-shadow:0px 6px 20px 10px rgba(0,0,0,0.3);");
        document.body.appendChild(bm);
    } else {
        bm.innerHTML = "";
    }
    bm.style.display = "block";
    var bmClose = document.createElement("a");
    bmClose.setAttribute("id", "bmClose");
    bmClose.setAttribute("href", "#");
    bmClose.setAttribute("style", "float:right; position:fixed; top:0; right:30px; display:block; font-size:2em; color:red");
    bmClose.innerHTML = "&times;"
    bm.appendChild(bmClose);

    bmClose.addEventListener("click", function(e) {
        document.getElementById("bmContainer").style.display = "none";

    }, false);

    document.addEventListener("click", function(e) {
        document.getElementById("bmContainer").style.display = "none";
    }, false);

    var bmIFrame = document.createElement("iframe");
    bmIFrame.setAttribute("id", "bmFrame");
    bmIFrame.setAttribute("name", "bmFrame");
    bmIFrame.setAttribute("width", "380px");
    bmIFrame.setAttribute("height", "420px");
    bmIFrame.setAttribute("frameborder", "0");
    bmIFrame.setAttribute("src", serverURL + "/ui.html?url="+encodeURIComponent(window.location.href)+'&title='+document.title);

    bm.appendChild(bmIFrame);
}

myBookmarklet();
