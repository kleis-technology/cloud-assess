{
  description = "Cloud Assess flake";
  inputs = {
    lcaac-flake.url = "github:kleis-technology/homebrew-lcaac/v2.0.0";
    nixpkgs.url = "github:nixos/nixpkgs/nixos-25.05";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = inputs@{ nixpkgs, lcaac-flake, flake-utils, ...}:
  flake-utils.lib.eachDefaultSystem (
    system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          name = "cloud-assess-dev-shell";
          packages = with pkgs; [
            jdk17
            gradle
            python312
            python312Packages.uv
            lcaac-flake.packages.aarch64-darwin.lcaac-cli
          ];
        };
      }
  );
}
