package com.google.common.truth.extension.generator.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Says "Hi" to the user.
 *
 */
@Mojo( name = "sayhi")
public class GreetingMojo extends AbstractMojo
{
  public void execute() throws MojoExecutionException
  {
    getLog().info( "Hello, world." );
  }
}
