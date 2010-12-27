import os
from google.appengine.ext.webapp import template
import cgi
from google.appengine.ext.webapp.util import login_required
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from google.appengine.api import mail

import config

class EmailHandler(webapp.RequestHandler):
    def post(self):
        
        secret_code = self.request.get("secret")
        if secret_code != config.SECRET_CODE:
            self.response.out.write('fail1')
            return
        
        to_addr = self.request.get("to")
        from_addr = self.request.get("from")
        body = self.request.get("message")
        subject = self.request.get("subject")
        
        if not mail.is_email_valid(to_addr):
            # Return an error message...
            self.response.out.write('fail2')
            return

        message = mail.EmailMessage()
        
        message.sender = from_addr
        message.to = to_addr
        message.subject = subject
        message.body = body

        message.send()
        
        #self.response.out.write('success')
        template_values = {}
        path = os.path.join(os.path.dirname(__file__), 'success.html')
        self.response.out.write(template.render(path, template_values))
        
class MainPage(webapp.RequestHandler):
    def get(self):
        template_values = {}
        path = os.path.join(os.path.dirname(__file__), 'index.html')
        self.response.out.write(template.render(path, template_values))



application = webapp.WSGIApplication(
                                     [
                                     ('/', MainPage),
                                     ('/email', EmailHandler),
                                      ],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
