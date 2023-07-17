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

	 */
	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
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
//		if(userList == null )
//		{
//			throw new NullPointerException("UserList is null");
//		}
		return membershipBackendClient.fetchMemberships()
				.thenCombine(usersList, (memberships, users) -> {
					if (users == null) {
						// Handle null users appropriately
						throw new IllegalStateException("Users list is null.");
					}

					Map<String, User> userMap = users.getUsers().stream()
							.collect(Collectors.toMap(User::getId, user -> user));

					for (Membership membership : memberships.getMemberships()) {
						String userId = membership.getUserId();
						if (userId != null) {
							User user = userMap.get(userId);
							membership.setUser(user);
						} else {
							// Handle null user ID appropriately
							throw new IllegalStateException("Membership user ID is null.");
						}
					}

					return memberships;
				});

	}
}
