
package com.example.versionplanner;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionRepo extends JpaRepository<Version, Long> {

	Version findByName(String name);

	@Query("DELETE FROM Version WHERE name NOT IN(SELECT version FROM Vote)")
	@Modifying
	@Transactional
	void deleteUnused();

}
