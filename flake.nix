{
  description = "Cloud Assess flake";
  inputs.lcaac-flake = {
    url = "github:kleis-technology/homebrew-lcaac/v1.8.0";
  };
  outputs = inputs@{ nixpkgs, lcaac-flake, ...}:
  {
    devShells = {
      aarch64-darwin.default =
      let
        pkgs = nixpkgs.legacyPackages.aarch64-darwin;
      in
      pkgs.mkShell {
        name = "cloud-assess-dev-shell";
    	packages = with pkgs; [
    	  jdk23
    	  gradle
    	  python312
    	  python312Packages.uv
    	  lcaac-flake.packages.aarch64-darwin.lcaac-cli
    	];
      };
    };
  };
}
