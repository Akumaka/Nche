const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotificaion = functions.database.ref("Notification/{notification_id}").onWrite((data, context) => {

  const notification_id = context.params.notification_id;

 //get the name . We'll be sending this in the payload
	const name = admin.database().ref(`Notification/${notification_id}/name`).once('value');
  const title = admin.database().ref(`Notification/${notification_id}/title`).once('value');

  return Promise.all([name, title]).then(result => {

      const senderName = result[0].val();
      const senderTitle = result[1].val();

      const payload = {
        notification:{
          title : "Complain generated",
          body : "Complain by -- " + senderName,
          click_action : "com.e.vicab.notification.push.by.server.target",
        },
        data : {
          name : senderName,
          notification_id : notification_id,
        }
      };

      return admin.messaging().sendToTopic("Notification", payload).then(response => {
        console.log('Notification Sent ---- ');
        return null;
      });

  });

});
