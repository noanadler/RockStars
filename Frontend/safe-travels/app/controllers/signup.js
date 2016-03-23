import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  session: Ember.inject.service(),

  actions: {
    signup: function() {
      var credentials = this.getProperties('email', 'password'),
        authenticator = 'authenticator:jwt',
        user = this.get('user'),
        session = this.get('session');

      //Ember.$.post(ENV.APP.apiUrl + '/signup', credentials).then(function() {
        session.authenticate(authenticator, { identification: credentials.email, password: credentials.password }).then(function() {
          //authenticated
          session.set('data.user', credentials.email );
        });
      //});
    }
  }
});
