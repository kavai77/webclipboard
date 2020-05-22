package com.himadri.webclipboard;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.NotFoundException;
import com.himadri.webclipboard.entity.Clipboard;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Controller
public class ClipboardController {
    @RequestMapping(method = RequestMethod.POST, value = "/copy")
    @ResponseBody
    public String copy(
        @RequestParam String userId,
        @RequestParam String text
    ) {
        ofy().save().entity(new Clipboard(userId, new Text(text), System.currentTimeMillis())).now();
        return "OK";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/paste")
    @ResponseBody
    public String paste(@RequestParam String userId) {
        try {
            return ofy().load().type(Clipboard.class).id(userId).safe().getText().getValue();
        } catch (NotFoundException e) {
            return "";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/_ah/warmup")
    @ResponseBody
    public String warmup() {
        return "OK";
    }
}
