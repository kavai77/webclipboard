$( document ).ready(function() {
    $("#login")
        .mouseenter(function() {
            $( this ).attr("src", "btn_google_signin_dark_pressed_web.png");
        })
        .mouseleave(function() {
            $( this ).attr("src", "btn_google_signin_dark_normal_web.png");
        })
        .focusin(function() {
            $( this ).attr("src", "btn_google_signin_dark_focus_web.png");
        })
        .focusout(function() {
            $( this ).attr("src", "btn_google_signin_dark_normal_web.png");
        })
        .click(function () {
            let provider = new firebase.auth.GoogleAuthProvider();
            firebase.auth().signInWithPopup(provider);
        });
    let wto;
    $("#area").on('input', function () {
        clearTimeout(wto);
        wto = setTimeout(function () { postData()}, 1000);
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
    $.ajaxSetup({
        beforeSend: function (xhr, settings)
        {
            firebase.auth().currentUser.getIdToken()
                .then(function(result) {
                    $.ajax($.extend(settings, {
                        headers: {"X-Authorization-Firebase": result},
                        beforeSend: $.noop
                    }));
                });
            return false;
        }
    });
    $('[data-toggle="tooltip"]').tooltip();
});

function postData() {
    $.ajax({
        url: "/text",
        type: "PUT",
        data: {
            text: $("#area").val()
        }
    });
}

function authStateObserver(user) {
    if (user) { // User is signed in!
        $("#loginContainer").hide();
        $("#avatar").prop('src', user.photoURL)
                    .prop('title', user.displayName)
                    .tooltip('_fixTitle');
        $("#loggedInUserContainer").show();
        $("#copyPasteArea").show();
        $("#removeButton").show();
        $.get({
            url: "/text",
            success: function (data) {
                $("#area").val(data);
                $("#area").select();
            }
        })
    } else { // User is signed out!
        $("#loginContainer").show();
        $("#loggedInUserContainer").hide();
        $("#copyPasteArea").hide();
        $("#removeButton").hide();
    }
}