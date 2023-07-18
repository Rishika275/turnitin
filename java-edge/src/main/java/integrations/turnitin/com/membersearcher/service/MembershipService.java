package integrations.turnitin.com.membersearcher.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.HashMap;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
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
	 *
	 */

	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
		CompletableFuture<UserList> userList = membershipBackendClient.fetchUsers();

		CompletableFuture<MembershipList> membershipListWithUsers = membershipBackendClient.fetchMemberships()
				.thenCombine(
						userList,
						(memberships, users) -> {
							HashMap<String, User> userMap = users.getUsers().stream()
									.collect(Collectors.toMap(User::getId, user -> user, (u1, u2) -> u1, HashMap::new));

							memberships.getMemberships().forEach(membership -> {
								User matchingUser = userMap.get(membership.getUserId());
								membership.setUser(matchingUser);
							});
							return memberships;
						}
				);
		return membershipListWithUsers;
	}
}
