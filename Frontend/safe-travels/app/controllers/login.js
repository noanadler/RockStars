import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  session: Ember.inject.service(),

  actions: {
    authenticate: function() {
      var credentials = this.getProperties('identification', 'password'),
        authenticator = 'authenticator:jwt',
        session = this.get('session');
      session.authenticate(authenticator, credentials).then(function(token) {
        session.set('data.user', credentials.identification );

        /*session.authorize('authorizer:token', (header, token) => {
          var headers = {}
          headers[header] = token;

          Ember.$.ajax({
            url: ENV.APP.apiUrl + '/testauth',
            headers: headers
          })
        });*/
      });
    }
  }
});
