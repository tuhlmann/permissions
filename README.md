# permissions

A library that handles user roles and permissions. 
Modeled after [Apache Shiro's](http://shiro.apache.org/permissions.html) WildcardPermission.

Please see the [JavaDoc for WildcardPermission](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/authz/permission/WildcardPermission.html)
for detailed information.

See [permissions_test.cljc](https://github.com/tuhlmann/permissions/blob/master/test/agynamix/permissions_test.cljc) for examples.

## Usage

A permission consists of three parts; a domain, a set of actions, and a set of entities.

If any of those is not given it defaults to a wildcard permission, or `"*"`.

A domain is usually an area in your application that this permission should be applicable for.
For instance if you have an area in your application managing user accounts, domain could be `"users"`.
You might have privileged users that are allowed to edit user accounts and those that can only read them.
So one permission could be `"users:read"` while other could receive `"users:edit"` (it's up to you how you name these
actions or how many you introduce.

Finally, you might want to allow some users only to edit some user records, that's where the third part, 
the entity list comes in. `"users:edit:12345,12346"` would require users to either name that entity in the list of entities
they are allowed to edit. Or alternatively as in the examples above, if you omit that field it becomes `"*"` the
wildcard permission. A wildcard permission in any of the three fields means the user has all permissions, 
either for all domains (first part, that's something only the super power root user should get), all actions for the
mentioned domain, or all entities for a given list of actions. I've really never before used the entities field to 
limit access to resources. But it's there if your use case requires it. 

The permissions library consists of two parts. The permissions namespace defines the low level API of permission and
the `implied-by?` method used to test if a resource permission (the firs parameter) is implied (has access to) 
by the second permission or list of permissions.

It also holds a factory method `make-permission` that should make it trivial to create a permission a la:

```
(make-permission "company") ;; allows all actions on domain 'company'
(make-permission "company" "read") ;; allows read action on domain 'company'
(make-permission "company:edit,update:123,124") ;; allows edit/update action on domain 'company' and entity 123/124
```


The second part is for conveniently interacting with maps that contain a set of roles and permissions.
It expects a key `:roles` and/or `:permissions` in the given map. It will construct a set of permissions by
pulling all permissions attached to the given roles and the single permissions found into one and then checks
if any of those permissions would allow the user to access the resources (as defined by the permission attached to that
resource).

```
(has-permission? user-map permission-or-string)

(lacks-permission? user-map permission-or-string)

```

In order to know how to resolve permissions from roles it has to be initialized with a 
map whose keys are the role names and the values are sets of permissions (either Permission records or strings)

A role map might look like this:

```
(def roles {"user/admin" "user:*"
            "user/all"   #{"user:read" "user:write"}
            "admin/all"  "*"
            "company/super" #{"company:read" "company:write" "company:edit" "company:delete"}
            }
```

          
Initialize the role mapping with:

```
(agynamix.roles/init-roles roles)
```

A user might look like this:

```         
(def user {:roles #{"user/all" "company/super"}
           :permissions #{"library:read" "company:gibberish"}
           ... lots of other keys
           }

```

Please have a look at [roles_test.cljc](https://github.com/tuhlmann/permissions/blob/master/test/agynamix/roles_test.cljc) for examples.


### Bitmask Permissions

Instead of attaching literal permission strings to a user record or to a resource you can also attach one or more numbers
that represent bitmasks. This works as follows:

In your application you need to define a mapping of role to permissions just as described above. But instead of
choosing keywords as names for roles you choose a number that is a multiple of two (that has only 1 bit set):


```
(def roles {1 "user:*"
            2   #{"user:read" "user:write"}
            4  "*"
            8 #{"company:read" "company:write" "company:edit" "company:delete"}
            }
```

You can map the number to one or more permissions, whatever suits your application.

Initialize the role mapping like so:

```
(:require [agynamix.roles :refer :all]
          [agynamix.bitmask-roles :refer :all])
            
(init-roles roles bitmask-permission-resolver bitmask-role-resolver)
```

It's important that you define the bitmap resolvers when initializing the role mapping!

Then in the user you declare the roles and permissions given to that user like so:

```         
(def user {:roles 10
           :permissions #{"library:read" "company:gibberish"}
           ... lots of other keys
           }

```

That number `10` sets the bits at positions `2` and `8`, thus role permissions given to the
roles `2` and `8` are added to that user.

In addition you can still set arbitrary permissions using the `:permissions` key in the user record if you want.
Or you could simple omit the key if it's not needed.

Similarly you give a resource one or more bitmask numbers. These numbers are reduced to the associated permissions
as described in the role mapping above. If they reduce to multiple permissions, then all resource permissions
must be met by the user (its like they are `and`ed together).


Please have a look at [bitmask_roles_test.cljc](https://github.com/tuhlmann/permissions/blob/master/test/agynamix/bitmask_roles_test.cljc) for examples.


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
