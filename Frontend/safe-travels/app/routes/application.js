import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import ApplicationRouteMixin from 'ember-simple-auth/mixins/application-route-mixin';

export default Ember.Route.extend(ApplicationRouteMixin, {
  //redirect() {
  //  this.transitionTo('signup');
  //}
});
