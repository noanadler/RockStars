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
          return Ember.Object.create({ id: country.country, text: country.full_name});
        });
      }),
      user: this.get('user').getCurrentUser()
    })
  },
  actions: {
    submit() {
      var headers = {}
      this.get('session').authorize('authorizer:token', (header, token) => {
        headers[header] = token;
      });

      //console.log(this.get('controller.model.user').toJSON());
      this.get('controller.model.user').set('countries', this.get('controller.countries').map(function(c) {
        return { country: c.id };
      }));

      Ember.$.ajax({
        url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
        method: 'PUT',
        headers: headers,
        contentType: "application/json; charset=utf-8",
        dataType:'json',
        data: this.get('controller.model.user').toJSON()
      })

      this.transitionTo('dashboard')
    }
  }
});
