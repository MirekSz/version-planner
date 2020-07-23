
package com.example.versionplanner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropRepo extends JpaRepository<Prop, Long> {

	public static final String RELEASER = "releaser";

	Prop findByName(String name);

}
