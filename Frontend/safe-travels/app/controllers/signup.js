import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  session: Ember.inject.service(),

  actions: {
    signup: function() {
      var credentials = this.getProperties('email', 'password', 'name'),
        authenticator = 'authenticator:jwt',
        user = this.get('user'),
        session = this.get('session'),
        controller = this;

      if(!credentials.email || !credentials.password || !credentials.name) {
        this.set('error', "All fields are required");
      } else {
        console.log('signing up');
        Ember.$.post(ENV.APP.apiUrl + '/signup', JSON.stringify(credentials)).then(function() {
          console.log('signed up');
          controller.set('success', true);
        }, function(response) {
          controller.set('error', response.responseText);
        });
      }
    }
  }
});
