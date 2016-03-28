import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  session: Ember.inject.service(),

  actions: {
    signup: function() {
      var credentials = this.getProperties('email', 'password'),
        authenticator = 'authenticator:jwt',
        user = this.get('user'),
        session = this.get('session'),
        controller = this;

      Ember.$.post(ENV.APP.apiUrl + '/signup', JSON.stringify(credentials)).then(function() {
        session.authenticate(authenticator, { identification: credentials.email, password: credentials.password }).then(function() {
          //authenticated
          session.authorize('authorizer:token', (header, token) => {
            var headers = {}
            headers[header] = token;

            Ember.$.ajax({
              url: ENV.APP.apiUrl + '/currentuuid',
              headers: headers
            }).then(function(data) {
              session.set('data.user', data );
              controller.transitionToRoute('setup');
            });
          });
        });
      });
    }
  }
});
