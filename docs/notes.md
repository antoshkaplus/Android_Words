On client side we don't want to support multiple users. One user should be enough.
NO WAY two guys would use one phone. And it's stupid to have multiple accounts for this kind of app.

BUT once user synchronized with one account and would like to synchronize with another we should
WARN him that he is using another account!!! but it shouldn't be harmful anyway


# Sync translation process:

### Server Side:
- client gets all updates after particular timestamp t_1
- client merges everything
- client sends his updates (should have timestamp later than t_1)
- server merges received updates
(later update always wins)

### Client Side:
- get timestamp of latest synchronization t_1
- create timestamp t_0
- get everything from the server after or equal t_1
- get everything from device after t_1
- merge, if device update later, pick that update and assign timestamp to t_0
(we want to extract and change in the same transaction)
- pick up everything after or at t_0 and send to the server
- assign last update to t_0

### Notes:
* To not miss client's stuff because of t_0 timestamp
client may decide to keep around flag if translation was updated or not
and pick up stuff that was not updated
* Client and Server sync process should be considered separately
and as much as possible independently
* For synchronization we will keep both: version and timestamp.
It eases out the whole process and may be helpful in the future.


# General sync process

Divided in two parts

**Update backend:**
* have to set update tables
* backend keeps track of update UUIDs
* try to get updates from update tables
* if nothing there
    * generate new update UUID
    * prepare updates
    * get updates out
    * we can leave if nothing is there
* if something there
    * check if update UUID in place
    * if not create new one
* send updates to server with specified UUID
    * server checks if UUID already successfully used
    * if used them server side does nothing
* if everything goes well and server reponded positively
    we reset update tables
* no reason to reset UUID. it will be done automatically

**Update local:**
* we keep last version of server db from which we've got update
* request from server updates with db version higher than what we have
* update everything locally



# Android docs

android predefined text sizes:


android:textAppearance="?android:attr/textAppearanceLarge"
android:textAppearance="?android:attr/textAppearanceMedium"
android:textAppearance="?android:attr/textAppearanceSmall"

fly is our dependency

never forget to call execute() with endpoints on client side
and now() on backend side

###some stuff from MainActivity layout
android:paddingLeft="@dimen/activity_horizontal_margin"
android:paddingRight="@dimen/activity_horizontal_margin"
android:paddingTop="@dimen/activity_vertical_margin"
android:paddingBottom="@dimen/activity_vertical_margin"

if you ask datastore for ids that are not there, it would return nothing

about backend:
if a single API is particularly complex you can use multiple API classes.
with same name and version strings for annotations.