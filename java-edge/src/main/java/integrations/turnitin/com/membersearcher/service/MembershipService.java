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
	 *
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all users
	 * and fetches all memberships.
	 * It then creates a HashMap( UserMap) with all the users where
	 * user_id is the key and the User object as value
	 * It iterates through the memberships and retrieves the associated user from the userMap
	 * based on the user ID in each membership.
	 * It updates the membership by setting the retrieved user as its associated user.
	 *
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
