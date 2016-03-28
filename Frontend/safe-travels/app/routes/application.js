import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import ApplicationRouteMixin from 'ember-simple-auth/mixins/application-route-mixin';

export default Ember.Route.extend(ApplicationRouteMixin, {
  session: Ember.inject.service('session'),
  beforeModel(transition) {
//    console.log(transition.targetName)
    //this._super()
    if(!this.get('session.isAuthenticated')) {
      this.transitionTo('signup');
    } else {
      if(transition.targetName == "index") {
        this.transitionTo('dashboard');
      }
    }
  }
});
