import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default Ember.Route.extend(AuthenticatedRouteMixin, {
  session: Ember.inject.service('session'),
  model() {
    return Ember.RSVP.hash({
      countries: Ember.$.get(ENV.APP.apiUrl + '/countries').then(function(response) {
        return response.map(function(country) {
          return Ember.Object.create({ id: country.country, text: country.full_name});
        });
      }),
      user: Ember.$.get(ENV.APP.apiUrl + '/user').then(function(user) {
        console.log(user)
      })
    })
  },
  actions: {
    submit() {
      this.transitionTo('dashboard')
    }
  }
});
