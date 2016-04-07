export default Ember.Controller.extend({
  session: Ember.inject.service('session'),
  //user: Ember.inject.service('user'),
  actions: {
  invalidateSession() {
    this.get('session').set('data.user', null );
    this.get('session').invalidate();
  }
}
});
