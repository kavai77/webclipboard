$( document ).ready(function() {
    $("#login")
        .mouseenter(function() {
            $( this ).attr("src", "btn_google_signin_dark_pressed_web.png");
        })
        .mouseleave(function() {
            $( this ).attr("src", "btn_google_signin_dark_normal_web.png");
        })
        .click(function () {
            let provider = new firebase.auth.GoogleAuthProvider();
            firebase.auth().signInWithPopup(provider);
        });
    let wto;
    $("#area").on('input', function () {
        clearTimeout(wto);
        wto = setTimeout(postData(), 500);
    });
    $("#logout").click(function () {
        firebase.auth().signOut();
    });
    $("#removeButton").click(function () {
        $("#area").val("");
        postData();
        $("#contentRemovedAlert").show();
    });
    $("#closeContentRemovedAlert").click(function () {
        $("#contentRemovedAlert").hide();
    });
    var firebaseConfig = {
        apiKey: "AIzaSyCTf_kuJxJH-pkMhNE2DKOp3rIDilfQyt8",
        authDomain: "webclipboard-7ac72.firebaseapp.com",
        databaseURL: "https://webclipboard-7ac72.firebaseio.com",
        projectId: "webclipboard",
        storageBucket: "webclipboard.appspot.com",
        messagingSenderId: "795338071888",
        appId: "1:795338071888:web:a348fa69b124228f551655"
    };
    firebase.initializeApp(firebaseConfig);
    firebase.auth().onAuthStateChanged(authStateObserver);
    $('[data-toggle="tooltip"]').tooltip();
});

function postData() {
    $.post({
        url: "/copy",
        data: {
            userId: firebase.auth().currentUser.uid,
            text: $("#area").val()
        }
    });
}

function authStateObserver(user) {
    if (user) { // User is signed in!
        $("#login").hide();
        $("#logout").show();
        $("#copyPasteArea").show();
        $("#removeButton").show();
        $.post({
            url: "/paste",
            data: {
                userId: user.uid,
            },
            success: function (data) {
                $("#area").val(data);
                $("#area").select();
            }
        })
    } else { // User is signed out!
        $("#login").show();
        $("#logout").hide();
        $("#copyPasteArea").hide();
        $("#removeButton").hide();
    }
}