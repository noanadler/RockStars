import Ember from 'ember';
import config from './config/environment';

var Router = Ember.Router.extend({
  location: config.locationType
});

Router.map(function() {
  this.route('signup');
  this.route('login');
  this.route('dashboard');
  this.route('setup');
  this.route('register', { path: '/register/:uuid' });
  this.route('noNotifications', { path: '/noNotifications/:uuid' });
});

export default Router;
