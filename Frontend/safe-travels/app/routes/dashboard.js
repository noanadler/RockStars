import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Route.extend({
  session: Ember.inject.service('session'),
  model() {
    return Ember.RSVP.hash({
      countries: this.get('session.data.user.countries').map(function(country) {
        Ember.$.get(ENV.APP.apiUrl + '/country/' + country.get('id'));
      }),
      user: Ember.$.get(ENV.APP.apiUrl + '/user').then(function(response) {

      }
    })
  }
});
