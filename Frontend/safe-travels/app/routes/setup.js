import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import User from 'safe-travels/models/user';
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default Ember.Route.extend(AuthenticatedRouteMixin, {
  session: Ember.inject.service('session'),
  user: Ember.inject.service('user'),

  model() {
    return Ember.RSVP.hash({
      countries: Ember.$.get(ENV.APP.apiUrl + '/countries').then(function(response) {
        return response.map(function(country) {
          return Ember.Object.create({ id: country.country, text: country.full_name });
        });
      }),
      user: this.get('user').getCurrentUser()
    })
  },
  actions: {
    submit() {
      var route = this;
      if(route.get('controller.countries.length')) {
        var headers = {}
        route.set('controller.error', false);
        this.get('session').authorize('authorizer:token', (header, token) => {
          headers[header] = token;
        });

        var json = route.get('controller.model.user').asJSON();
        json.countries = route.get('controller.countries').map(function(c) {
          return c.id;
        });

        Ember.$.ajax({
          url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
          method: 'PUT',
          headers: headers,
          contentType: "application/json; charset=utf-8",
          dataType:'json',
          data: JSON.stringify(json),
        }).then(function() {
          route.get('user').loadUser().then(function() {
            route.transitionTo('dashboard')
          });
        })
      } else {
        route.set('controller.error', true);
      }
    }
  }
});
