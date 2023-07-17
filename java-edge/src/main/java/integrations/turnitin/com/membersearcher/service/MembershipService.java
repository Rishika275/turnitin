package integrations.turnitin.com.membersearcher.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.Membership;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships,
	 * it then calls to fetch the user details for each user individually and
	 * associates them with their corresponding membership.
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList object.

	 return membershipBackendClient.fetchMemberships()
				.thenCompose(members -> {
					CompletableFuture<?>[] userCalls = members.getMemberships().stream()
							.map(member -> membershipBackendClient.fetchUser(member.getUserId())
									.thenApply(member::setUser))
							.toArray(CompletableFuture<?>[]::new);
					return CompletableFuture.allOf(userCalls)
							.thenApply(nil -> members);
				});
	//		CompletableFuture<UserList> usersList = membershipBackendClient.fetchUsers();
	//
	//		return membershipBackendClient.fetchMemberships()
	//				.thenCombine(usersList, (memberships, users) -> {
	//					Map<String, User> userMap = users.getUsers().stream()
	//							.collect(Collectors.toMap(User::getId, user -> user));
	//
	//					for (Membership membership : memberships.getMemberships()) {
	//						User user = userMap.get(membership.getUserId());
	//						membership.setUser(user);
	//					}
	//
	//					return memberships;
	//				});

	CompletableFuture<UserList> usersList = membershipBackendClient.fetchUsers();
		return membershipBackendClient.fetchMemberships()
				.thenCombine(usersList, (memberships, users) -> {
							Map<String, User> userMap = users.getUsers().stream()
									.collect(Collectors.toMap(User::getId, user -> user));

              CompletableFuture<Void>[] members = memberships.getMemberships().stream()
                  .map(membership -> {
                      User user = userMap.get(membership.getUserId());
                      membership.setUser(user);
                      return CompletableFuture.completedFuture(null);
                  })
                  .toArray(CompletableFuture[]::new);

                  return CompletableFuture.allOf(members)
                  .thenApply(nil -> memberships);
					});
	 ================
	CompletableFuture<UserList> usersList = membershipBackendClient.fetchUsers();
	return membershipBackendClient.fetchMemberships()
	.thenCombine(usersList, (memberships, users) -> {
	Map<String, User> userMap = users.getUsers().stream()
	.collect(Collectors.toMap(User::getId, user -> user));
	memberships.getMemberships().forEach(membership -> {
	User user = userMap.get(membership.getUserId());
	membership.setUser(user);
	});
	return memberships;
	});


	 */
	private CompletableFuture<UserList> getAllUsers(){
		return membershipBackendClient.fetchUsers();
	}
//	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
//		CompletableFuture<UserList> usersList = getAllUsers();
//		return membershipBackendClient.fetchMemberships()
//				.thenCombine(usersList, (memberships, users) -> {
//					Map<String, User> userMap = users.getUsers().stream()
//							.collect(Collectors.toMap(User::getId, user -> user));
//					memberships.getMemberships().forEach(membership -> {
//						User user = userMap.get(membership.getUserId());
//						membership.setUser(user);
//					});
//					CompletableFuture<?>[] members = memberships.getMemberships().stream()
//							.map(membership -> CompletableFuture.completedFuture(membership))
//							.toArray(CompletableFuture[]::new);
//
//					return CompletableFuture.allOf(members)
//							.thenApply(nil -> memberships);
//				});
//	}

	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
		Map<String, User> usersMapFuture = membershipBackendClient.fetchUsers()
				.thenCompose(users -> {})
				.thenApply(users -> users.stream().collect(Collectors.toMap(User::getId, user -> user)));

		return membershipBackendClient.fetchMemberships()
				.thenCompose(members -> {
					CompletableFuture<?>[] userCalls = members.getMemberships().stream()
							.map(member -> usersMapFuture.get(member.getUserId())
									.thenApply(member::setUser))
							.toArray(CompletableFuture<?>[]::new);
					return CompletableFuture.allOf(userCalls)
							.thenApply(nil -> members);
				});
	}
}
